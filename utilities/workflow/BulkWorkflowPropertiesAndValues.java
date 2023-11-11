package workflow;

import requests.GetTargetGroupByCode;
import requests.WorkflowAPI;
import requests.CredentialAPI;
import requests.EnvironmentAPI;
import requests.TargetAPI;

import pojo.PropertyDefinitionPojo;
import pojo.CredentialScopeEnum;

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
import java.util.stream.*;

import java.io.*;

class TGThread extends Thread
{
  // in
  private TargetAPI tAPI;
  private String targetGroupCode;

  // out
  public String targetGroupId;

  public TGThread(TargetAPI tAPI, String targetGroupCode)
  {
    this.tAPI = tAPI;
    this.targetGroupCode = targetGroupCode;
  }

  public void run()
  {
    try
    {
      JSONArray targetGroupsArray = tAPI.findTargetGroupByCode(targetGroupCode);
      JSONObject targetGroupObject = parseTargetGroupsArray(targetGroupCode, targetGroupsArray);
      targetGroupId = targetGroupObject.get("targetGroupId").toString();
    }
    catch (FlexCheckedException fce)
    {
      throw new RuntimeException(fce);
    }
  }

  private JSONObject parseTargetGroupsArray(String pTargetGroupCode, JSONArray pJsonArray)
    throws FlexCheckedException
  {
    final String methodName = "parseTargetGroupsArray";
    LOGGER.entering(CLZ_NAM, methodName, new Object[]{pTargetGroupCode, pJsonArray});

    if (pJsonArray.length() == 0)
    {
      throw new FlexCheckedException("Target Group not found with targetGroupCode " + pTargetGroupCode);
    }

    JSONObject targetGroupObject = null;
    for (int i = 0; i < pJsonArray.length(); i++)
    {
      JSONObject current = pJsonArray.getJSONObject(i);
      if (pTargetGroupCode.equals(current.getString("targetGroupCode")))
      {
        targetGroupObject = current;
        break;
      }
    }

    if (targetGroupObject == null)
    {
      throw new FlexCheckedException("Target Group not found with code " + pTargetGroupCode);
    }
    
    LOGGER.exiting(CLZ_NAM, methodName, targetGroupObject);
    return targetGroupObject;
  }
}

class CSThread extends Thread
{
  // in
  private CredentialAPI credAPI;

  // out
  public String localCredStoreId;
  public String localCredStoreInputDefId;

  public CSThread(CredentialAPI credAPI)
  {
    this.credAPI = credAPI;
  }

  public void run()
  {
    try
    {
      JSONArray storesArray = credAPI.getLocalCredentialStore();
      JSONObject localCredentialStoreObject = parseLocalCredentialStoreArray(storesArray);
      localCredStoreId = localCredentialStoreObject.get("credentialStoreId").toString();
      String localCredStoreDefId = localCredentialStoreObject.get("credentialStoreDefId").toString();
      JSONObject localCredStoreProviderObject = credAPI.getLocalCredentialStoreProvider(localCredStoreDefId);
      localCredStoreInputDefId = localCredStoreProviderObject.getJSONArray("credentialStoreInputDefs").getJSONObject(0).get("credentialStoreInputDefId").toString();
    }
    catch (FlexCheckedException fce)
    {
      throw new RuntimeException(fce);
    }
  }

  private JSONObject parseLocalCredentialStoreArray(JSONArray pJsonArray)
    throws FlexCheckedException
  {
    final String methodName = "parseLocalCredentialStoreArray";
    LOGGER.entering(CLZ_NAM, methodName, pJsonArray);

    if (pJsonArray.length() == 0)
    {
      throw new FlexCheckedException("Local credential store not found");
    }

    JSONObject credStoreObject = null;
    for (int i = 0; i < pJsonArray.length(); i++)
    {
      JSONObject current = pJsonArray.getJSONObject(i);
      if ("Local".equals(current.getString("credentialStoreName")))
      {
        credStoreObject = current;
        break;
      }
    }

    if (credStoreObject == null)
    {
      throw new FlexCheckedException("Local credential store not found");
    }
    
    LOGGER.exiting(CLZ_NAM, methodName, credStoreObject);
    return credStoreObject;
  }
}
 
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
  protected static String INPUT_CSV_FILE_PATH;
  protected static String WORKFLOW_SOURCE;

  private static String targetGroupId;
  private static String localCredStoreId;
  private static String localCredStoreInputDefId;
  private static List<String> targetEnvironmentCodes = new ArrayList<>();
  private static Map<String, String> codeToValue = new HashMap<>(); //key is code+environmentCode, value is target property value
  private static Map<String, String> credentialNameToValue = new HashMap<>(); //key is credentialName_targetGroupCode_environmentCode, value is credential value
  private static Map<String, String> credentialNameToId = new HashMap<>(); //key is credentialName_targetGroupCode_environmentCode, value is credential id
  private static Map<String, String> environmentCodeToEnvironmentId = new HashMap<>();

  private static WorkflowAPI wfAPI;
  private static EnvironmentAPI envAPI;
  private static CredentialAPI credAPI;
  private static TargetAPI tAPI;

  public static void main(String[] args)
    throws FlexCheckedException
  {
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		LOGGER.addHandler(consoleHandler);
		LOGGER.setLevel(Level.ALL);
		LOGGER.setUseParentHandlers(false);

    if (args == null || args.length < 7)
    {
      throw new IllegalArgumentException("BASE_URL, USERNAME, PASSWORD, WORKFLOW_NAME, TARGET_GROUP_CODE, INPUT_CSV_FILE_PATH, and WORKFLOW_SOURCE must be passed as arguments.");
    }
    BASE_URL = args[0];
    USERNAME = args[1];
    PASSWORD = args[2];
    WORKFLOW_NAME = args[3];
    TARGET_GROUP_CODE = args[4];
    INPUT_CSV_FILE_PATH = args[5];
    WORKFLOW_SOURCE = args[6];

    wfAPI = new WorkflowAPI(BASE_URL, USERNAME, PASSWORD);
    envAPI = new EnvironmentAPI(BASE_URL, USERNAME, PASSWORD);
    credAPI =  new CredentialAPI(BASE_URL, USERNAME, PASSWORD);
    tAPI = new TargetAPI(BASE_URL, USERNAME, PASSWORD);

    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
    System.out.println("//////////////////////////////////////////////////PREREQUISITE DATA///////////////////////////////////////////////////////////////////////////////////////");
    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");

    TGThread tgt = new TGThread(tAPI, TARGET_GROUP_CODE);
    CSThread cs = new CSThread(credAPI);
    tgt.start();
    cs.start();
    tgt.join();
    cs.join();

    targetGroupId = tgt.targetGroupId;
    localCredStoreId = cs.localCredStoreId;
    localCredStoreInputDefId = cs.localCredStoreInputDefId;

    LOGGER.fine("Target Group Id: " + targetGroupId);
    LOGGER.fine("Local Credential Store Id: " + localCredStoreId);
    LOGGER.fine("Local Credential Store Secret Text Definition Id: " + localCredStoreInputDefId);

    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
    System.out.println("//////////////////////////////////////////////////CREATE/UPDATE WORKFLOW_PROPERTIES///////////////////////////////////////////////////////////////////////");
    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
    
    JSONArray workflowsArray = wfAPI.findWorkflowByName(WORKFLOW_NAME);
    JSONObject workflowObject = validateWorkflowArray(workflowsArray);
    workflowObject.put("sourceCode", WORKFLOW_SOURCE); // this is required for update workflow and get workflow does not return the value
    workflowObject.put("sourceCodeURL", "dummy"); // this is required or validation will fail - sourceCodeURL is not actually used in backend

    JSONArray workflowPropertiesJSONArray = workflowObject.getJSONArray("properties");
    List<PropertyDefinitionPojo> existingWorkflowProperties = PropertyDefinitionPojo.convertObjectsToPropertyDefinition(workflowPropertiesJSONArray);

    File csv = new File(INPUT_CSV_FILE_PATH);
    List<String> lines = FlexFileUtils.read(csv);
    List<PropertyDefinitionPojo> incomingWorkflowProperties = readAndProcessCSV(lines);
    List<PropertyDefinitionPojo> mergedWorkflowProperties = mergeWorkflowProperties(existingWorkflowProperties, incomingWorkflowProperties);
    writeWorkflowPropertiesToWorkflowObject(workflowObject, mergedWorkflowProperties);

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

    // parallel loop to create or update credentials
    credentialNameToValue.keySet().stream().parallel().forEach(credentialName ->
      {
        try
        {
          JSONArray searchResult = credAPI.findCredentialByName(credentialName);
          JSONObject credentialObject = validateCredentialArray(credentialName, searchResult);
          String credentialValue = credentialNameToValue.get(credentialName);
          String credentialId;
          if (credentialObject == null)
          {
            LOGGER.info("Creating credential " + credentialName);
            // create
            JSONObject postCredentialRequestBody = new JSONObject();
            postCredentialRequestBody.put("credentialName", credentialName);
            postCredentialRequestBody.put("isActive", true); // defaulting
            postCredentialRequestBody.put("credentialScope", CredentialScopeEnum.PROPERTY); // defaulting to PROPERTY ("ENVINST" same difference)
            postCredentialRequestBody.put("credentialStoreId", localCredStoreId);

            JSONArray inputs = new JSONArray();
            JSONObject input = new JSONObject();
            input.put("inputValue", credentialValue);
            input.put("credentialStoreInputDefId", localCredStoreInputDefId);
            inputs.put(input);
            postCredentialRequestBody.put("credentialInputs", inputs);

            credentialId = credAPI.createCredential(postCredentialRequestBody.toString()).get("credentialId").toString();
          }
          else
          {
            // update - override inputValue only
            credentialId = credentialObject.get("credentialId").toString();
            LOGGER.info("Updating credential with id " + credentialId + " and credential name " + credentialName);
            credentialObject.getJSONArray("credentialInputs").getJSONObject(0).put("inputValue", credentialValue);
            credAPI.patchCredentialById(credentialId, credentialObject.toString());
          }

          credentialNameToId.put(credentialName, credentialId);
        }
        catch (FlexCheckedException fce)
        {
          throw new RuntimeException(fce);
        }
      }
    );

    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
    System.out.println("///////////////////////////////////////////////////////UPDATE TARGETS//////////////////////////////////////////////////////////////////////////");
    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
    
    // parallel loop to update target properties
    mergedWorkflowProperties.stream().parallel().forEach(prop ->
      {
        try
        {
          boolean isEncrypted = prop.getIsEncrypted();
          String name = prop.getName();

          LOGGER.info("Patching target property " + name + " (encrypted=" + isEncrypted + ")");
          for (String environmentCode : targetEnvironmentCodes)
          {
            String environmentId = environmentCodeToEnvironmentId.get(environmentCode);
            String targetValue = codeToValue.get(name + environmentCode);

            JSONArray propertiesArray = new JSONArray();
            JSONObject property = new JSONObject();
            property.put("propertyName", name);

            if (isEncrypted)
            {
              String credentialName = String.format("%s_%s_%s", name, TARGET_GROUP_CODE, environmentCode);
              property.put("credentialId", credentialNameToId.get(credentialName));
            }
            else 
            {
              property.put("propertyValue", targetValue);
            }
            propertiesArray.put(property);

            JSONObject patchRequestBody = new JSONObject();
            patchRequestBody.put("properties", propertiesArray);

            tAPI.patchTargetById(environmentId, targetGroupId, patchRequestBody.toString());
          }
        }
        catch (FlexCheckedException fce)
        {
          throw new RuntimeException(fce);
        }
      }
    );
  }

  /**
   * Write properties to Workflow JsonObject
   */
  private static void writeWorkflowPropertiesToWorkflowObject(JSONObject workflowObject, List<PropertyDefinitionPojo> properties)
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
          String credentialName = String.format("%s_%s_%s", name, TARGET_GROUP_CODE, environmentCode);
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

  private static void validateEnvironmentsMappedToTargetGroup(List<String> pErrorList)
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
        pErrorList.add("Environment " + environmentCode + " is not mapped to target group " + TARGET_GROUP_CODE + " but referenced in CSV file header. Change header in CSV file or map environment to target group");
      }
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
        // will only have target property values if scope is ENVINST
        for (int j = 0; j < numEnvironments; j++)
        {
          String key = code + targetEnvironmentCodes.get(j);
          String value = tokens[j+15]; //important to add 15 here
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
}
