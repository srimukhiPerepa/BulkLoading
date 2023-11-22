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
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import java.io.*;

/**
 * Thread to read CSV lines and validate each row
 * Also return helper lists/maps for quick lookup of values. This way CSV is only read once.
 */
public class ReaderValidatorThread extends Thread
{
  private final String CLZ_NAM = ReaderValidatorThread.class.getName();
  private final Logger LOGGER = Logger.getGlobal();

  // in
  private TargetAPI tAPI;
  private PropertyAPI pAPI;
  private EnvironmentAPI envAPI;
  private String targetGroupCode;
  private String targetGroupId;
  private String workflowName;
  private List<String> lines;

  // out
  public Exception exception;
  public List<String> targetEnvironmentCodes = new ArrayList<>();
  public Map<String, String> codeToValue = new HashMap<>(); //key is code+environmentCode, value is target property value
  public Map<String, String> credentialNameToValue = new HashMap<>(); //key is credentialName_targetGroupCode_environmentCode, value is credential value
  public Map<String, String> environmentCodeToEnvironmentId = new HashMap<>();
  public List<PropertyKeyDefinitionDataObject> incomingPropertyKeyDefinitions; // PropertyKeyDefinitions only from CSV
  public JSONObject propertySetObject;

  public ReaderValidatorThread(TargetAPI tAPI, PropertyAPI pAPI, EnvironmentAPI envAPI, String targetGroupCode, 
                                String targetGroupId, String workflowName, List<String> lines)
  {
    this.tAPI = tAPI;
    this.pAPI = pAPI;
    this.envAPI = envAPI;
    this.targetGroupCode = targetGroupCode;
    this.targetGroupId = targetGroupId;
    this.workflowName = workflowName;
    this.lines = lines;
  }

  public void run()
  {
    try
    {
      LOGGER.info("Valdating workflowName " + workflowName + " points to one and only one PropertySet");
      JSONArray propertySetArray = pAPI.findPropertySetByName(workflowName);
      propertySetObject = validateWorkflowPropertySetArray(propertySetArray);

      LOGGER.info("Validating header in CSV file: " + lines.get(0));
      List<String> errors = new ArrayList<>();
      String[] headers = lines.get(0).split(",");
      // startIdx is the start of ENVIRONMENT_CODE columns - After LENGTH column
      int startIdx = IntStream.range(0, headers.length)
                        .filter(i -> headers[i].toUpperCase().equals("LENGTH"))
                        .findFirst()
                        .orElse(headers.length);
      startIdx++;
      LOGGER.finest("startIdx: " + startIdx);

      for (int i = startIdx; i < headers.length; i++)
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

      LOGGER.info("Validating rest of CSV...");
      incomingPropertyKeyDefinitions = readAndProcessCSV(startIdx, lines);

      LOGGER.finest("targetEnvironmentCodes map: " + codeToValue);
      LOGGER.finest("codeToValue map: " + codeToValue);
      LOGGER.finest("credentialNameToValue map: " + credentialNameToValue);
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

  private List<PropertyKeyDefinitionDataObject> readAndProcessCSV(int startIdx, List<String> pLines)
    throws FlexCheckedException
  {
    final String methodName = "readAndProcessCSV";
    LOGGER.entering(CLZ_NAM, methodName, startIdx);

    List<PropertyKeyDefinitionDataObject> results = new ArrayList<>();
    List<String> errors = new ArrayList<>();

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
      String defaultValue = tokens[16];
      String isDefaultExpression = tokens[17];
      String length = tokens[18];

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

      // If TARGET (ENVINST) property, keep track of:
      //  propertyKeyName->value for each environment
      //  credentialName->credentialValue for each environment
      if ("ENVINST".equals(propertyScope))
      {
        if (tokens.length < (startIdx + numEnvironments))
        {
          LOGGER.warning("Line " + i + " is missing target values. Missing values will be set to empty string");
        }

        String key, environmentCode;
        for (int j = 0; j < numEnvironments; j++)
        {
          environmentCode = targetEnvironmentCodes.get(j);
          key = propertyKeyName + environmentCode;
          String value = "";
          try
          {
            value = tokens[j+startIdx];
          }
          catch (ArrayIndexOutOfBoundsException aio)
          {
            //ignore
          }

          if (Boolean.valueOf(isEncrypted))
          {
            String credentialName = String.format("%s_%s_%s", propertyKeyName, targetGroupCode, environmentCode);
            credentialNameToValue.put(credentialName, value);
          }
          else
          {
            codeToValue.put(key, value);
          }
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

      results.add(pojo);
    }

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
}
