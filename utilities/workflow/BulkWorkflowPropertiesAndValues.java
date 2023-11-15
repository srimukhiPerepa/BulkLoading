package workflow;

import requests.WorkflowAPI;
import requests.CredentialAPI;
import requests.EnvironmentAPI;
import requests.TargetAPI;

import pojo.PropertyDefinitionPojo;
import pojo.CredentialScopeEnum;

import threads.*;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.utils.FlexCommonUtils;
import flexagon.ff.common.core.logging.FlexLogger;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.*;
import java.util.stream.*;

import java.io.*;

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
  private static List<String> targetEnvironmentCodes;
  private static Map<String, String> codeToValue; //key is code+environmentCode, value is target property value
  private static Map<String, String> credentialNameToValue; //key is credentialName_targetGroupCode_environmentCode, value is credential value
  private static Map<String, String> environmentCodeToEnvironmentId;
  private static Map<String, String> credentialNameToId = new HashMap<>(); //key is credentialName_targetGroupCode_environmentCode, value is credential id

  private static WorkflowAPI wfAPI;
  private static EnvironmentAPI envAPI;
  private static CredentialAPI credAPI;
  private static TargetAPI tAPI;

  public static void main(String[] args)
    throws Exception
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

    validate();

    wfAPI = new WorkflowAPI(BASE_URL, USERNAME, PASSWORD);
    envAPI = new EnvironmentAPI(BASE_URL, USERNAME, PASSWORD);
    credAPI =  new CredentialAPI(BASE_URL, USERNAME, PASSWORD);
    tAPI = new TargetAPI(BASE_URL, USERNAME, PASSWORD);

    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
    System.out.println("//////////////////////////////////////////////////PREREQUISITE DATA///////////////////////////////////////////////////////////////////////////////////////");
    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");

    TGThread tg = new TGThread(tAPI, TARGET_GROUP_CODE);
    CSThread cs = new CSThread(credAPI);
    tg.start();
    cs.start();
    tg.join();

    if (tg.exception != null)
    {
      throw new Exception(tg.exception.getMessage());
    }

    targetGroupId = tg.targetGroupId;
    LOGGER.fine("Target Group Id: " + targetGroupId);

    // WFThread depends on tg thread to complete
    WFThread wf = new WFThread(tAPI, wfAPI, envAPI, TARGET_GROUP_CODE, targetGroupId, WORKFLOW_NAME, WORKFLOW_SOURCE, INPUT_CSV_FILE_PATH);
    wf.start();
    cs.join();

    if (cs.exception != null)
    {
      throw new Exception(cs.exception.getMessage());
    }

    wf.join();

    if (wf.exception != null)
    {
      throw new Exception(wf.exception.getMessage());
    }

    localCredStoreId = cs.localCredStoreId;
    localCredStoreInputDefId = cs.localCredStoreInputDefId;
    targetEnvironmentCodes = wf.targetEnvironmentCodes;
    codeToValue = wf.codeToValue;
    credentialNameToValue = wf.credentialNameToValue;
    environmentCodeToEnvironmentId = wf.environmentCodeToEnvironmentId;

    LOGGER.fine("Local Credential Store Id: " + localCredStoreId);
    LOGGER.fine("Local Credential Store Secret Text Definition Id: " + localCredStoreInputDefId);

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

    int index = 1;
    int total = credentialNameToValue.size();
    for (String credentialName: credentialNameToValue.keySet())
    {
      JSONArray searchResult = credAPI.findCredentialByName(credentialName);
      JSONObject credentialObject = validateCredentialArray(credentialName, searchResult);
      String credentialValue = credentialNameToValue.get(credentialName);
      String credentialId;

      LOGGER.info("Creating/updating credential " + credentialName + " " + (index++) + " of " + total);
      if (credentialObject == null)
      {
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
        credentialObject.getJSONArray("credentialInputs").getJSONObject(0).put("inputValue", credentialValue);
        credAPI.patchCredentialById(credentialId, credentialObject.toString());
      }

      credentialNameToId.put(credentialName, credentialId);
    }

    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
    System.out.println("///////////////////////////////////////////////////////UPDATE TARGETS//////////////////////////////////////////////////////////////////////////");
    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
    
    index = 1;
    total = wf.mergedWorkflowProperties.size();
    for (PropertyDefinitionPojo prop : wf.mergedWorkflowProperties)
    {
      boolean isEncrypted = prop.getIsEncrypted();
      String name = prop.getName();
      String scope = prop.getScope();

      if (!"ENVINST".equals(scope))
      {
        LOGGER.info("Skipping " + scope + " property " + name + " - " + (index++) + " of " + total);
        continue;
      }

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
          // If credential is not CSV then credentialName will not be a key in credentialNameToId map
          if (!credentialNameToId.containsKey(credentialName))
          {
            continue;
          }
          LOGGER.info("Patching target property " + name + " (credential) - " + (index++) + " of " + total);
          property.put("credentialId", credentialNameToId.get(credentialName));
        }
        else 
        {
          LOGGER.info("Patching target property " + name + " - " + (index++) + " of " + total);
          property.put("propertyValue", targetValue);
        }
        propertiesArray.put(property);

        JSONObject patchRequestBody = new JSONObject();
        patchRequestBody.put("properties", propertiesArray);

        tAPI.patchTargetById(environmentId, targetGroupId, patchRequestBody.toString());
      }
    }
  }

  private static void validate()
    throws Exception
  {
    final String methodName = "validate";
    LOGGER.entering(CLZ_NAM, methodName);

    if (FlexCommonUtils.isEmpty(BASE_URL) || FlexCommonUtils.isEmpty(USERNAME) || FlexCommonUtils.isEmpty(PASSWORD) || FlexCommonUtils.isEmpty(WORKFLOW_NAME)
        || FlexCommonUtils.isEmpty(TARGET_GROUP_CODE) || FlexCommonUtils.isEmpty(INPUT_CSV_FILE_PATH) || FlexCommonUtils.isEmpty(WORKFLOW_SOURCE))
    {
      throw new RuntimeException("BASE_URL, USERNAME, PASSWORD, WORKFLOW_NAME, TARGET_GROUP_CODE, INPUT_CSV_FILE_PATH, and WORKFLOW_SOURCE cannot be empty");
    }
    
    // WORKFLOW_SOURCE cannot have ` special character or will fail
    if (WORKFLOW_SOURCE.contains("`"))
    {
      throw new RuntimeException("WORKFLOW_SOURCE cannot contain the special character `");
    }

    LOGGER.exiting(CLZ_NAM, methodName);
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
}
