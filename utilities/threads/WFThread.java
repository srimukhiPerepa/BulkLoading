package threads;

import requests.TargetAPI;
import requests.PropertyAPI;
import requests.EnvironmentAPI;

import pojo.PropertyKeyDefinitionDataObject;
import pojo.PropertySetKeyDefDataObject;
import pojo.PropertyTypeEnum;

import threads.*;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.utils.FlexCommonUtils;
import flexagon.ff.common.core.utils.FlexFileUtils;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import java.io.*;

public class WFThread extends Thread
{
  private final String CLZ_NAM = WFThread.class.getName();
  private final Logger LOGGER = Logger.getGlobal();

  // in
  private TargetAPI tAPI;
  private PropertyAPI pAPI;
  private EnvironmentAPI envAPI;
  private String targetGroupCode;
  private String targetGroupId;
  private String workflowName;
  private String csvFilePath;

  // out
  public Exception exception;
  public List<PropertyKeyDefinitionDataObject> mergedPropertyKeyDefinitions = new ArrayList<>();
  public List<String> targetEnvironmentCodes = new ArrayList<>();
  public Map<String, String> codeToValue = new HashMap<>(); //key is code+environmentCode, value is target property value
  public Map<String, String> credentialNameToValue = new HashMap<>(); //key is credentialName_targetGroupCode_environmentCode, value is credential value
  public Map<String, String> environmentCodeToEnvironmentId = new HashMap<>();

  public WFThread(TargetAPI tAPI, PropertyAPI pAPI, EnvironmentAPI envAPI, String targetGroupCode, 
                  String targetGroupId, String workflowName, String csvFilePath)
  {
    this.tAPI = tAPI;
    this.pAPI = pAPI;
    this.envAPI = envAPI;
    this.targetGroupCode = targetGroupCode;
    this.targetGroupId = targetGroupId;
    this.workflowName = workflowName;
    this.csvFilePath = csvFilePath;
  }

  public void run()
  {
    try
    {
      // Check if property set exists with the passed workflowName
      JSONArray propertySetArray = pAPI.findPropertySetByName(workflowName);
      JSONObject propertySetObject = validateWorkflowPropertySetArray(propertySetArray);
      String propertySetId = propertySetObject.get("propertySetId").toString();

      JSONArray propertySetKeyDefsJSONArray = propertySetObject.getJSONArray("propertySetKeyDefs");
      List<PropertySetKeyDefDataObject> existingPropertySetKeyDefs = PropertySetKeyDefDataObject.convertJSONArrayToObjects(propertySetKeyDefsJSONArray);
      for (PropertySetKeyDefDataObject propertySet : existingPropertySetKeyDefs)
      {
        String propertyDefinitionId = propertySet.getPropertyDefinitionId().toString();
        JSONObject response = pAPI.getPropertyKeyDefinitionById(propertyDefinitionId);
        mergedPropertyKeyDefinitions.add(PropertyKeyDefinitionDataObject.fromJson(response));
      }

      File csv = new File(csvFilePath);
      List<String> lines = FlexFileUtils.read(csv);
      List<PropertyKeyDefinitionDataObject> incomingPropertyKeyDefinitions = readAndProcessCSV(lines);
      int index = 1;
      for (PropertyKeyDefinitionDataObject propKeyDef : incomingPropertyKeyDefinitions)
      {
        String propertyKeyName = propKeyDef.getPropertyKeyName();
        JSONArray searchResult = pAPI.findPropertyKeyDefinitionByName(propertyKeyName);

        LOGGER.info("Creating/updating property key definition " + propertyKeyName + " " + (index++) + " of " + incomingPropertyKeyDefinitions.size());
        if (searchResult.length() == 0)
        {
          // create
          JSONObject requestBody = propKeyDef.toJson();
          JSONObject response = pAPI.createPropertyKeyDefinition(requestBody.toString());
          // update mergedPropertyKeyDefinitions
          mergedPropertyKeyDefinitions.add(PropertyKeyDefinitionDataObject.fromJson(response));
          propertyKeyDef.setPropertyDefinitionId(response.getLong("propertyDefinitionId"));
        }
        else
        {
          // patch
          String propertyKeyDefinitionId = searchResult.getJSONObject(0).get("propertyDefinitionId").toString();
          JSONObject requestBody = propKeyDef.toJson();
          pAPI.patchPropertyKeyDefinitionById(propertyKeyDefinitionId, requestBody.toString());
          // update mergedPropertyKeyDefinitions
          mergedPropertyKeyDefinitions.set(index - 2, propKeyDef);
          propertyKeyDef.setPropertyDefinitionId(Long.valueOf(propertyKeyDefinitionId));
        }

        // update propertyKeyDefinitionId for incomingPropertyKeyDefinitions
        incomingPropertyKeyDefinitions.set(index - 2, propKeyDef);
      }

      writeWorkflowPropertySetKeyDefs(propertySetObject, mergeWorkflowPropertySets(propertySetId, existingPropertySetKeyDefs, incomingPropertyKeyDefinitions));
      pAPI.updatePropertySetById(propertySetId, propertySetObject.toString());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      exception = ex;
    }

    LOGGER.info(CLZ_NAM + " completed successfully");
  }

    /**
   * Validates pJsonArray contains only one JSONObject and return the JSONObject
   * pJsonArray - Array of JSONObject containing Workflow Definitions
   */
  private JSONObject validateWorkflowPropertySetArray(JSONArray pJsonArray)
    throws FlexCheckedException
  {
    final String methodName = "validateWorkflowPropertySetArray";
    LOGGER.entering(CLZ_NAM, methodName, pJsonArray);

    if (pJsonArray.length() == 0)
    {
      throw new FlexCheckedException("No workflow(s) found with name " + workflowName);
    }

    if (pJsonArray.length() > 1)
    {
      throw new FlexCheckedException("More than one workflow found with name " + workflowName + ". WORKFLOW_NAME must be unique.");
    }

    JSONObject wfObject = pJsonArray.getJSONObject(0);
    String propertySetName = wfObject.getString("propertySetName");
    if (!workflowName.equals(propertySetName))
    {
      throw new FlexCheckedException("Workflow Name " + workflowName + " does not exactly match propertySetName " + propertySetName);
    }

    LOGGER.exiting(CLZ_NAM, methodName, wfObject);
    return wfObject;
  }

  private List<PropertyKeyDefinitionDataObject> readAndProcessCSV(List<String> pLines)
    throws FlexCheckedException
  {
    final String methodName = "readAndProcessCSV";
    LOGGER.entering(CLZ_NAM, methodName, pLines);

    List<PropertyKeyDefinitionDataObject> results = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    String[] headers = pLines.get(0).split(",");
    for (int i = 20; i < headers.length; i++)
    {
      String environmentCode = headers[i];
      validateEnvironmentCode(environmentCode, errors);
      targetEnvironmentCodes.add(environmentCode);
    }

    validateEnvironmentsMappedToTargetGroup(errors);

    if (errors.size() > 0)
    {
      throw new FlexCheckedException(errors.toString());
    }
    LOGGER.finest("environmentCodeToEnvironmentId map: " + environmentCodeToEnvironmentId);

    int numEnvironments = targetEnvironmentCodes.size();
    int numLines = pLines.size();
    for (int i = 1; i < numLines; i++)
    {
      String line = pLines.get(i);
      String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
      int tokensLength = tokens.length;
      String propertyKeyName = tokens[0];
      String propertyScope = tokens[1];
      String propertyKeyDataType = tokens[2];
      String isRequired = tokens[3];
      String isEncrypted = tokens[4];
      String isActive = tokens[5];
      String displayName = tokens[6];
      String description = tokens[7];
      String propertyKeySubDataType = tokens[8];
      String minValue = tokens[9];
      String maxValue = tokens[10];
      String listData = tokens[11];
      String isMultiselect = tokens[12];
      String displayRows = tokens[13];
      String displayColumns = tokens[14];
      String validator1 = tokens[15];
      String dataType = tokens[16];
      String defaultValue = tokens[17];
      String isDefaultExpression = tokens[18];
      String length = tokens[19];

      PropertyKeyDefinitionDataObject pojo = new PropertyKeyDefinitionDataObject();
      // required excluding propertyDefinitionId
      pojo.setPropertyKeyName(propertyKeyName);
      pojo.setPropertyScope(propertyScope);
      pojo.setPropertyKeyDatatype(PropertyTypeEnum.valueOf(propertyKeyDataType));
      pojo.setIsRequired(Boolean.valueOf(isRequired));
      pojo.setIsEncrypted(Boolean.valueOf(isEncrypted));
      pojo.setIsActive(Boolean.valueOf(isActive));
      pojo.setIsAllowsVariant(true); // defaulting to true on create

      // optional
      if (displayName != null)
      {
        pojo.setDisplayName(displayName);
      }
      if (description != null)
      {
        pojo.setDescription(description);
      }
      if (propertyKeySubDataType != null)
      {
        pojo.setPropertyKeySubDatatype(propertyKeySubDataType);
      }
      if (FlexCommonUtils.isNotEmpty(minValue))
      {
        pojo.setMinValue(Long.valueOf(minValue));
      }
      if (FlexCommonUtils.isNotEmpty(maxValue))
      {
        pojo.setMaxValue(Long.valueOf(maxValue));
      }
      if (listData != null)
      {
        pojo.setListData(listData);
      }
      if (FlexCommonUtils.isNotEmpty(isMultiselect))
      {
        pojo.setIsMultiselect(Boolean.valueOf(isMultiselect));
      }
      if (FlexCommonUtils.isNotEmpty(displayRows))
      {
        pojo.setDisplayRows(Integer.valueOf(displayRows));
      }
      if (FlexCommonUtils.isNotEmpty(displayColumns))
      {
        pojo.setDisplayColumns(Integer.valueOf(displayColumns));
      }
      if (validator1 != null)
      {
        pojo.setValidator1(validator1);
      }
      if (defaultValue != null)
      {
        pojo.setDefaultValue(defaultValue);
      }
      if (FlexCommonUtils.isNotEmpty(isDefaultExpression))
      {
        pojo.setIsDefaultExpression(Boolean.valueOf(isDefaultExpression));
      }
      if (FlexCommonUtils.isNotEmpty(length))
      {
        pojo.setLength(Long.valueOf(length));
      }

      results.add(pojo);

      if ("ENVINST".equals(propertyScope))
      {
        if (tokens.length < (20 + numEnvironments))
        {
          LOGGER.warning("Line " + i + " is missing target values. Missing values will be set to empty string");
        }

        // will only have target property values if scope is ENVINST
        for (int j = 0; j < numEnvironments; j++)
        {
          String key = propertyKeyName + targetEnvironmentCodes.get(j);
          // important to add 20 here which is after LENGTH column
          String value = "";
          try
          {
            value = tokens[j+20];
          }
          catch (ArrayIndexOutOfBoundsException aio)
          {
            //ignore
          }
          codeToValue.put(key, value);
        }
      }

      if (FlexCommonUtils.isEmpty(propertyKeyName))
      {
        errors.add("Line " + i + " is missing PROPERTY_KEY_NAME");
      }


      if (FlexCommonUtils.isEmpty(propertyScope) || !(propertyScope.equals("ENVINST") || propertyScope.equals("PROJECT")))
      {
        errors.add("Line " + i + " PROPERTY_SCOPE must be ENVINST or PROJECT");
      }

      if (FlexCommonUtils.isEmpty(isRequired))
      {
        errors.add("Line " + i + " is missing IS_REQUIRED");
      }

      if (FlexCommonUtils.isEmpty(isEncrypted))
      {
        errors.add("Line " + i + " is missing IS_ENCRYPTED");
      }

      if (FlexCommonUtils.isEmpty(isActive))
      {
        errors.add("Line " + i + " is missing IS_ACTIVE");
      }

      if (FlexCommonUtils.isEmpty(propertyKeyDataType))
      {
        errors.add("Line " + i + " is missing PROPERTY_KEY_DATA_TYPE");
      }

      if (FlexCommonUtils.isNotEmpty(propertyKeySubDataType) && !(propertyKeySubDataType.equals("DIRECTORY") || propertyKeySubDataType.equals("JDBCURL") || propertyKeySubDataType.equals("URL")))
      {
        errors.add("Line " + i + " PROPERTY_KEY_SUB_DATA_TYPE must be DIRECTORY, JDBCURL or URL");
      }
    }
    LOGGER.finest("codeToValue mapping: " + codeToValue);

    if (errors.size() > 0)
    {
      throw new FlexCheckedException(errors.toString());
    }

    LOGGER.exiting(CLZ_NAM, methodName, results.size());
    return results;
  }

  private void validateEnvironmentCode(String pEnvironmentCode, List<String> pErrorList)
    throws FlexCheckedException
  {
    final String methodName = "validateEnvironmentCode";
    LOGGER.entering(CLZ_NAM, methodName, pEnvironmentCode);

    LOGGER.info("Validating environment with code: " + pEnvironmentCode);
    JSONArray result = envAPI.findEnvironmentByCode(pEnvironmentCode);
    if (result.length() == 0)
    {
      // fail
      pErrorList.add("Environment was not found with environment code " + pEnvironmentCode + ". Fix header in CSV file");
    }
    else
    {
      // success - environment exists
      String environmentId = result.getJSONObject(0).get("environmentId").toString();
      environmentCodeToEnvironmentId.put(pEnvironmentCode, environmentId);
    }   

    LOGGER.exiting(CLZ_NAM, methodName);
  }

  private void validateEnvironmentsMappedToTargetGroup(List<String> pErrorList)
    throws FlexCheckedException
  {
    final String methodName = "validateEnviromentsMappedToTargetGroup";
    LOGGER.entering(CLZ_NAM, methodName);

    JSONObject targetGroupObject = tAPI.getTargetGroupById(targetGroupId);
    JSONArray targetsArray = targetGroupObject.getJSONArray("targets");
    // convert targetsArray to a List so we can use it with Streaming API
    List<JSONObject> converted = IntStream.range(0, targetsArray.length())
                                  .mapToObj(i -> targetsArray.getJSONObject(i))
                                  .collect(Collectors.toList());
    for (String environmentCode : targetEnvironmentCodes)
    {
      String environmentId = environmentCodeToEnvironmentId.get(environmentCode);
      boolean isMapped = converted.stream().anyMatch(json -> json.get("environmentId").toString().equals(environmentId));
      if (!isMapped)
      {
        pErrorList.add("Environment " + environmentCode + " is not mapped to target group " + targetGroupCode + " but referenced in CSV file header. Change header in CSV file or map environment to target group");
      }
    }

    LOGGER.exiting(CLZ_NAM, methodName);
  }

  /**
   * Merge both lists with incomingWorkflowProperties taking precedence if there are duplicates
   */
  private List<PropertySetKeyDefDataObject> mergeWorkflowPropertySets(String propertySetId, List<PropertySetKeyDefDataObject> existing, List<PropertyKeyDefinitionDataObject> incoming)
  {
    final String methodName = "mergeWorkflowPropertySets";
    LOGGER.entering(CLZ_NAM, methodName, new Object[]{propertySetId, existing, incoming});

    List<PropertySetKeyDefDataObject> merged = new ArrayList<>(existing);
    for (int i = 0; i < incoming.size(); i++)
    {
      PropertyKeyDefinitionDataObject propKeyDefPojo = incoming.get(i);
      // Keep track of the workflow properties which are encrypted and store in credentialNameToValue
      if (propKeyDefPojo.getIsEncrypted())
      {
        String name = propKeyDefPojo.getPropertyKeyName().trim();
        if (name.endsWith("_"))
        {
          name = name.substring(0, name.length() - 1);
        }
        for (String environmentCode: targetEnvironmentCodes)
        {
          String key = propKeyDefPojo.getPropertyKeyName() + environmentCode;
          String credentialName = String.format("%s_%s_%s", name, targetGroupCode, environmentCode);
          credentialNameToValue.put(credentialName, codeToValue.get(key));
        }
      }

      PropertySetKeyDefDataObject tempPropSetKey = new PropertySetKeyDefDataObject();
      tempPropSetKey.setPropertySetId(Long.valueOf(propertySetId));
      tempPropSetKey.setPropertyDefinitionId(propKeyDefPojo.getPropertyDefinitionId());

      int index = merged.indexOf(tempPropSetKey);
      if (index != -1)
      {
        LOGGER.info("Workflow Property Key Definition with code " + propKeyDefPojo.getPropertyKeyName() + " already exists in the workflow. Overriding values.");
        merged.set(index, tempPropSetKey);
      }
      else
      {
        LOGGER.info("Adding new Workflow Property Key Definition with code " + propKeyDefPojo.getPropertyKeyName());
        merged.add(tempPropSetKey);
      }
    }

    LOGGER.exiting(CLZ_NAM, methodName, merged);
    return merged;
  }

  /**
   * Write properties to PropertSet JsonObject
   */
  private void writeWorkflowPropertySetKeyDefs(JSONObject propertySetObject, List<PropertySetKeyDefDataObject> properties)
  {
    final String methodName = "writeWorkflowPropertySetKeyDefs";
    LOGGER.entering(CLZ_NAM, methodName);

    propertySetObject.put("propertySetKeyDefs", new JSONArray()); // clears existing properties
    for (PropertySetKeyDefDataObject pojo : properties)
    {
      propertySetObject.getJSONArray("propertySetKeyDefs").put(pojo.toJson());
    }
    LOGGER.info("Final Workflow Property Set Object: " + propertySetObject.toString(2));

    LOGGER.exiting(CLZ_NAM, methodName);
  }
}