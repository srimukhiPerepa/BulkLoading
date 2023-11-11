package workflow;

import requests.WorkflowAPI;
import requests.CredentialAPI;
import requests.EnvironmentAPI;
import requests.TargetAPI;

import pojo.PropertyDefinitionPojo;
import pojo.CredentialScopeEnum;

import threads.*;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
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

    wfAPI = new WorkflowAPI(BASE_URL, USERNAME, PASSWORD);
    envAPI = new EnvironmentAPI(BASE_URL, USERNAME, PASSWORD);
    credAPI =  new CredentialAPI(BASE_URL, USERNAME, PASSWORD);
    tAPI = new TargetAPI(BASE_URL, USERNAME, PASSWORD);

    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
    System.out.println("//////////////////////////////////////////////////PREREQUISITE DATA///////////////////////////////////////////////////////////////////////////////////////");
    System.out.println("//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");

    WFThread wf = new WFThread(wfAPI, WORKFLOW_NAME, WORKFLOW_SOURCE, INPUT_CSV_FILE_PATH);
    TGThread tg = new TGThread(tAPI, TARGET_GROUP_CODE);
    CSThread cs = new CSThread(credAPI);
    wf.start();
    tg.start();
    cs.start();
    tg.join();
    cs.join();
    wf.join();

    targetGroupId = tg.targetGroupId;
    localCredStoreId = cs.localCredStoreId;
    localCredStoreInputDefId = cs.localCredStoreInputDefId;

    LOGGER.fine("Target Group Id: " + targetGroupId);
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
}
