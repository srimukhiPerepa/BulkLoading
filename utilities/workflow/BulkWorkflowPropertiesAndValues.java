package workflow;

import requests.GetTargetGroupByCode;
import requests.WorkflowAPI;
import requests.CredentialAPI;
import requests.EnvironmentAPI;

import pojo.PropertyDefinitionPojo;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.logging.FlexLogger;
import flexagon.ff.common.core.rest.FlexRESTClientResponse;
import flexagon.ff.common.core.utils.FlexCommonUtils;
import flexagon.ff.common.core.utils.FlexFileUtils;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.*;

import java.io.File;
 
public class BulkWorkflowPropertiesAndValues
{
  private static final String CLZ_NAM = BulkWorkflowPropertiesAndValues.class.getName();
  private static final FlexLogger LOG = FlexLogger.getLogger(CLZ_NAM);
  private static final Logger LOGGER = Logger.getGlobal();

  protected static String BASE_URL;
  protected static String USERNAME;
  protected static String PASSWORD;
  protected static String WORKFLOW_NAME;
  protected static String TARGET_GROUP_CODE;
  protected static String WORKFLOW_SOURCE;

  private static List<String> targetEnvironmentCodes = new ArrayList<>();
  private static Map<String, String> codeToValue = new HashMap<>(); //key is code##environment_code, value is target property value
  private static Map<String, String> credentialNameToValue = new HashMap<>(); //key is credential name, value is credential value
  private static Map<String, String> environmentCodeToEnvironmentId = new HashMap<>();

  private static EnvironmentAPI envAPI;

  public static void main(String[] args)
    throws FlexCheckedException
  {
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		LOGGER.addHandler(consoleHandler);
		LOGGER.setLevel(Level.ALL);
		LOGGER.setUseParentHandlers(false);

    if (args == null || args.length < 6)
    {
      throw new IllegalArgumentException("BASE_URL, USERNAME, PASSWORD, WORKFLOW_NAME, TARGET_GROUP_CODE, and WORKFLOW_SOURCE must be passed as arguments.");
    }
    BASE_URL = args[0];
    USERNAME = args[1];
    PASSWORD = args[2];
    WORKFLOW_NAME = args[3];
    TARGET_GROUP_CODE = args[4];
    WORKFLOW_SOURCE = args[5];

    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
    System.out.println("//////////////////////////////////////////////////CREATE/UPDATE WORKFLOW_PROPERTIES///////////////////////////////////////////////////////////////////////");
    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
    
    WorkflowAPI wfAPI = new WorkflowAPI(BASE_URL, USERNAME, PASSWORD);
    envAPI = new EnvironmentAPI(BASE_URL, USERNAME, PASSWORD);
    JSONArray workflowsArray = wfAPI.findWorkflowByName(WORKFLOW_NAME);
    JSONObject workflowObject = validateWorkflowArray(workflowsArray);
    workflowObject.put("sourceCode", WORKFLOW_SOURCE); // this is required for update workflow and get workflow does not return the value
    workflowObject.put("sourceCodeURL", "dummy"); // this is required or validation will fail - sourceCodeURL is not actually used in backend

    JSONArray workflowPropertiesJSONArray = workflowObject.getJSONArray("properties");
    List<PropertyDefinitionPojo> existingWorkflowProperties = PropertyDefinitionPojo.convertObjectsToPropertyDefinition(workflowPropertiesJSONArray);

    File csv = new File("../examples/workflow_property_values.csv");
    List<String> lines = FlexFileUtils.read(csv);
    List<PropertyDefinitionPojo> incomingWorkflowProperties = readAndProcessCSV(lines);
    List<PropertyDefinitionPojo> mergedWorkflowProperties = mergeWorkflowProperties(existingWorkflowProperties, incomingWorkflowProperties);

    // Write merged results to workflowObject
    workflowObject.put("properties", new JSONArray()); // clears existing properties
    for (PropertyDefinitionPojo pojo : mergedWorkflowProperties)
    {
      workflowObject.getJSONArray("properties").put(pojo.toJson());
    }
    LOGGER.info("Final Workflow Object: " + workflowObject.toString(2));

    String workflowId = workflowObject.get("workflowId").toString();
    wfAPI.updateWorkflowById(workflowId, workflowObject.toString());

    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
    System.out.println("///////////////////////////////////////////////////////CREATE/UPDATE CREDENTIALS//////////////////////////////////////////////////////////////////////////");
    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
    
    if (credentialNameToValue.size() == 0)
    {
      LOGGER.info("No credentials found to create/update");
    }
    else 
    {
      LOGGER.info(credentialNameToValue.size() + " to create/update");
      LOGGER.fine("credentialNameToValue map: " + credentialNameToValue);
    }
    CredentialAPI credAPI = new CredentialAPI(BASE_URL, USERNAME, PASSWORD);
    for (String credentialName : credentialNameToValue.keySet())
    {
      JSONArray searchResult = credAPI.findCredentialByName(credentialName);
      JSONObject credential = validateCredentialArray(credentialName, searchResult);
      if (credential == null)
      {
        // create
      }
      else
      {
        // update
      }
    }
  }

  /**
   * Merge both lists with incomingWorkflowProperties taking precedence if there are duplicates
   */
  private static List<PropertyDefinitionPojo> mergeWorkflowProperties(List<PropertyDefinitionPojo> existing, List<PropertyDefinitionPojo> incoming)
  {
    final String methodName = "mergeWorkflowProperties";
    LOGGER.entering(CLZ_NAM, methodName, new Object[]{existing, incoming});

    List<PropertyDefinitionPojo> merged = new ArrayList<>(existing);
    for (int i = 0; i < incoming.size(); i++)
    {
      PropertyDefinitionPojo pojo = incoming.get(i);
      // Keep track of the workflow properties which are encrypted and store
      if (pojo.getIsEncrypted())
      {
        String credentialName = pojo.getName().trim();
        if (credentialName.endsWith("_"))
        {
          credentialName = credentialName.substring(0, credentialName.length() - 1);
        }
        for (String environmentCode: targetEnvironmentCodes)
        {
          String key = pojo.getName() + environmentCode;
          credentialNameToValue.put(credentialName + "_" + environmentCode, codeToValue.get(key));
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
   * Validates pJsonArray contains zero or more than one JSONObject(s) and return the JSONObject or null
   * pJsonArray - Array of JSONObject containing Credentials
   */
  private static JSONObject validateCredentialArray(String pCredentialName, JSONArray pJsonArray)
    throws FlexCheckedException
  {
    final String methodName = "validateCredentialArray";
    LOGGER.entering(CLZ_NAM, methodName, new Object[]{pCredentialName, pJsonArray});

    if (pJsonArray.length() > 1)
    {
      throw new FlexCheckedException("More than one credential found with name " + pCredentialName + ". Credential Name must be unique.");
    }

    if (pJsonArray.length() == 0)
    {
      LOGGER.exiting(CLZ_NAM, methodName);
      return null;
    }

    JSONObject wfObject = pJsonArray.getJSONObject(0);
    LOGGER.exiting(CLZ_NAM, methodName, wfObject);
    return wfObject;
  }

    /**
   * Validates pJsonArray contains only one JSONObject and return the JSONObject
   * pJsonArray - Array of JSONObject containing Workflow Definitions
   */
  private static JSONObject validateWorkflowArray(JSONArray pJsonArray)
    throws FlexCheckedException
  {
    final String methodName = "validateWorkflowArray";
    LOGGER.entering(CLZ_NAM, methodName, pJsonArray);

    if (pJsonArray.length() == 0)
    {
      throw new FlexCheckedException("No workflow(s) found with name " + WORKFLOW_NAME);
    }

    if (pJsonArray.length() > 1)
    {
      throw new FlexCheckedException("More than one workflow found with name " + WORKFLOW_NAME + ". WORKFLOW_NAME must be unique.");
    }

    JSONObject wfObject = pJsonArray.getJSONObject(0);

    LOGGER.exiting(CLZ_NAM, methodName, wfObject);
    return wfObject;
  }

  private static void validateEnvironmentCode(String pEnvironmentCode, List<String> pErrorList)
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

  private static List<PropertyDefinitionPojo> readAndProcessCSV(List<String> pLines)
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

      for (int j = 0; j < numEnvironments; j++)
      {
        String key = code + targetEnvironmentCodes.get(j);
        String value = tokens[j+15]; //important to add 15 here
        codeToValue.put(key, value);
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
}
