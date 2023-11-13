package threads;

import requests.TargetAPI;
import requests.WorkflowAPI;
import requests.EnvironmentAPI;

import pojo.PropertyDefinitionPojo;

import threads.*;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.utils.FlexCommonUtils;
import flexagon.ff.common.core.utils.FlexFileUtils;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.*;

import java.io.*;
import java.util.stream.*;

public class WFThread extends Thread
{
  private final String CLZ_NAM = WFThread.class.getName();
  private final Logger LOGGER = Logger.getGlobal();

  // in
  private TargetAPI tAPI;
  private WorkflowAPI wfAPI;
  private EnvironmentAPI envAPI;
  private String targetGroupCode;
  private String targetGroupId;
  private String workflowName;
  private String workflowSource;
  private String csvFilePath;

  // out
  public Exception exception;
  public List<PropertyDefinitionPojo> mergedWorkflowProperties = new ArrayList<>();
  public List<String> targetEnvironmentCodes = new ArrayList<>();
  public Map<String, String> codeToValue = new HashMap<>(); //key is code+environmentCode, value is target property value
  public Map<String, String> credentialNameToValue = new HashMap<>(); //key is credentialName_targetGroupCode_environmentCode, value is credential value
  public Map<String, String> environmentCodeToEnvironmentId = new HashMap<>();

  public WFThread(TargetAPI tAPI, WorkflowAPI wfAPI, EnvironmentAPI envAPI, String targetGroupCode, 
                  String targetGroupId, String workflowName, String workflowSource, String csvFilePath)
  {
    this.tAPI = tAPI;
    this.wfAPI = wfAPI;
    this.envAPI = envAPI;
    this.targetGroupCode = targetGroupCode;
    this.targetGroupId = targetGroupId;
    this.workflowName = workflowName;
    this.workflowSource = workflowSource;
    this.csvFilePath = csvFilePath;
  }

  public void run()
  {
    try
    {
      JSONArray workflowsArray = wfAPI.findWorkflowByName(workflowName);
      JSONObject workflowObject = validateWorkflowArray(workflowsArray);
      // FlexCommonUtils.buildStringFromChunks seems to replace " with \" which would cause updateWorkflow to fail because
      // sourceXML we are passing in does not match value stored in database. We'll replace that now.

      workflowSource = workflowSource.replace("\\\"", "\"");
      workflowObject.put("sourceCode", workflowSource); // this is required for update workflow and get workflow does not return the value
      workflowObject.put("sourceCodeURL", "dummy"); // this is required or validation will fail - sourceCodeURL is not actually used in backend

      JSONArray workflowPropertiesJSONArray = workflowObject.getJSONArray("properties");
      List<PropertyDefinitionPojo> existingWorkflowProperties = PropertyDefinitionPojo.convertObjectsToPropertyDefinition(workflowPropertiesJSONArray);

      File csv = new File(csvFilePath);
      List<String> lines = FlexFileUtils.read(csv);
      List<PropertyDefinitionPojo> incomingWorkflowProperties = readAndProcessCSV(lines);
      mergedWorkflowProperties = mergeWorkflowProperties(existingWorkflowProperties, incomingWorkflowProperties);
      writeWorkflowPropertiesToWorkflowObject(workflowObject, mergedWorkflowProperties);

      String workflowId = workflowObject.get("workflowId").toString();
      wfAPI.updateWorkflowById(workflowId, workflowObject.toString());
    }
    catch (Exception ex)
    {
      exception = ex;
    }

    LOGGER.info(CLZ_NAM + " completed successfully");
  }

    /**
   * Validates pJsonArray contains only one JSONObject and return the JSONObject
   * pJsonArray - Array of JSONObject containing Workflow Definitions
   */
  private JSONObject validateWorkflowArray(JSONArray pJsonArray)
    throws FlexCheckedException
  {
    final String methodName = "validateWorkflowArray";
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

    LOGGER.exiting(CLZ_NAM, methodName, wfObject);
    return wfObject;
  }

  private List<PropertyDefinitionPojo> readAndProcessCSV(List<String> pLines)
    throws FlexCheckedException
  {
    final String methodName = "readAndProcessCSV";
    LOGGER.entering(CLZ_NAM, methodName, pLines);

    List<PropertyDefinitionPojo> results = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    String[] headers = pLines.get(0).split(",");
    for (int i = 15; i < headers.length; i++)
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
      String code = tokens[0];
      String displayName = tokens[1];
      String propertyScope = tokens[2];
      String isRequired = tokens[3];
      String dataType = tokens[4];
      String isEncrypted = tokens[5];
      String displayRows = tokens[6];
      String displayColumns = tokens[7];
      String listData = tokens[8];
      String subDataType = tokens[9];
      String isDefaultValueExpression = tokens[10];
      String isMultiselect = tokens[11];
      String description = tokens[12];
      String isActive = tokens[13];
      String defaultValue = tokens[14];

      PropertyDefinitionPojo pojo = new PropertyDefinitionPojo();
      pojo.setIsEncrypted(Boolean.valueOf(isEncrypted));
      pojo.setIsRequired(Boolean.valueOf(isRequired));
      pojo.setIsDefaultValueExpression(isDefaultValueExpression != null ? Boolean.valueOf(isDefaultValueExpression) : false);
      pojo.setIsMultiselect(isMultiselect != null ? Boolean.valueOf(isMultiselect) : false);
      pojo.setIsActive(isActive != null ? Boolean.valueOf(isActive) : true);
      pojo.setDataType(dataType);
      pojo.setScope(propertyScope);
      pojo.setName(code);
      pojo.setDisplayRows(FlexCommonUtils.isNotEmpty(displayRows) ? Integer.parseInt(displayRows.toString()) : null);
      pojo.setDisplayColumns(FlexCommonUtils.isNotEmpty(displayColumns) ? Integer.parseInt(displayColumns.toString()) : null);
      pojo.setListData(FlexCommonUtils.isNotEmpty(listData) ? Arrays.asList(listData.toString().trim().split(",")) : null);
      pojo.setSubDataType(FlexCommonUtils.isNotEmpty(subDataType) ? subDataType.toString() : null);
      pojo.setDisplayName(FlexCommonUtils.isNotEmpty(displayName) ? displayName.toString() : null);
      pojo.setDescription(FlexCommonUtils.isNotEmpty(description) ? description.toString() : null);
      pojo.setDefaultValue(FlexCommonUtils.isNotEmpty(defaultValue) ? defaultValue.toString() : null);

      results.add(pojo);

      if ("ENVINST".equals(propertyScope))
      {
        if (tokens.length < (15 + numEnvironments))
        {
          LOGGER.warning("Line " + i + " is missing target values. Missing values will be set to empty string");
        }

        // will only have target property values if scope is ENVINST
        for (int j = 0; j < numEnvironments; j++)
        {
          String key = code + targetEnvironmentCodes.get(j);
          // important to add 15 here which is after DEFAULT_VALUE column
          String value = "";
          try
          {
            value = tokens[j+15];
          }
          catch (ArrayIndexOutOfBoundsException aio)
          {
            //ignore
          }
          codeToValue.put(key, value);
        }
      }

      if (FlexCommonUtils.isEmpty(code))
      {
        errors.add("Line " + i + " is missing CODE");
      }

      if (FlexCommonUtils.isEmpty(displayName))
      {
        errors.add("Line " + i + " is missing DISPLAY_NAME");
      }

      if (FlexCommonUtils.isEmpty(propertyScope) || !(propertyScope.equals("ENVINST") || propertyScope.equals("PROJECT")))
      {
        errors.add("Line " + i + " PROPERTY_SCOPE must be ENVINST or PROJECT");
      }

      if (FlexCommonUtils.isEmpty(isRequired))
      {
        errors.add("Line " + i + " is missing REQUIRED");
      }

      if (FlexCommonUtils.isEmpty(dataType))
      {
        errors.add("Line " + i + " is missing DATA_TYPE");
      }

      if (FlexCommonUtils.isNotEmpty(subDataType) && !(subDataType.equals("DIRECTORY") || subDataType.equals("JDBCURL") || subDataType.equals("URL")))
      {
        errors.add("Line " + i + " SUB_DATA_TYPE must be DIRECTORY, JDBCURL or URL");
      }

      if (FlexCommonUtils.isEmpty(isEncrypted))
      {
        errors.add("Line " + i + " is missing ENCRYPTED");
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
  private List<PropertyDefinitionPojo> mergeWorkflowProperties(List<PropertyDefinitionPojo> existing, List<PropertyDefinitionPojo> incoming)
  {
    final String methodName = "mergeWorkflowProperties";
    LOGGER.entering(CLZ_NAM, methodName, new Object[]{existing, incoming});

    List<PropertyDefinitionPojo> merged = new ArrayList<>(existing);
    for (int i = 0; i < incoming.size(); i++)
    {
      PropertyDefinitionPojo pojo = incoming.get(i);
      // Keep track of the workflow properties which are encrypted and store in credentialNameToValue
      if (pojo.getIsEncrypted())
      {
        String name = pojo.getName().trim();
        if (name.endsWith("_"))
        {
          name = name.substring(0, name.length() - 1);
        }
        for (String environmentCode: targetEnvironmentCodes)
        {
          String key = pojo.getName() + environmentCode;
          String credentialName = String.format("%s_%s_%s", name, targetGroupCode, environmentCode);
          credentialNameToValue.put(credentialName, codeToValue.get(key));
        }
      }

      int index = merged.indexOf(pojo);
      if (index != -1)
      {
        LOGGER.info("Workflow Property with code " + pojo.getName() + " already exists in the workflow. Overriding values.");
        merged.set(index, pojo);
      }
      else
      {
        LOGGER.info("Adding new Workflow Property with code " + pojo.getName());
        merged.add(pojo);
      }
    }

    LOGGER.exiting(CLZ_NAM, methodName, merged);
    return merged;
  }

  /**
   * Write properties to Workflow JsonObject
   */
  private void writeWorkflowPropertiesToWorkflowObject(JSONObject workflowObject, List<PropertyDefinitionPojo> properties)
  {
    final String methodName = "writeWorkflowPropertiesToWorkflowObject";
    LOGGER.entering(CLZ_NAM, methodName);

    workflowObject.put("properties", new JSONArray()); // clears existing properties
    for (PropertyDefinitionPojo pojo : properties)
    {
      workflowObject.getJSONArray("properties").put(pojo.toJson());
    }
    LOGGER.info("Final Workflow Object: " + workflowObject.toString(2));

    LOGGER.exiting(CLZ_NAM, methodName);
  }
}