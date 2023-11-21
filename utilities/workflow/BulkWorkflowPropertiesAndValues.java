package workflow;

import requests.PropertyAPI;
import requests.CredentialAPI;
import requests.EnvironmentAPI;
import requests.TargetAPI;

import pojo.PropertyKeyDefinitionDataObject;
import pojo.CredentialScopeEnum;

import threads.*;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.utils.FlexCommonUtils;
import flexagon.ff.common.core.logging.FlexLogger;
import flexagon.ff.common.core.utils.FlexFileUtils;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.*;

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

  private static PropertyAPI pAPI;
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

    if (args == null || args.length < 6)
    {
      throw new IllegalArgumentException("BASE_URL, USERNAME, PASSWORD, WORKFLOW_NAME, TARGET_GROUP_CODE and INPUT_CSV_FILE_PATH must be passed as arguments.");
    }
    BASE_URL = args[0];
    USERNAME = args[1];
    PASSWORD = args[2];
    WORKFLOW_NAME = args[3];
    TARGET_GROUP_CODE = args[4];
    INPUT_CSV_FILE_PATH = args[5];

    validate();

    pAPI = new PropertyAPI(BASE_URL, USERNAME, PASSWORD);
    envAPI = new EnvironmentAPI(BASE_URL, USERNAME, PASSWORD);
    credAPI =  new CredentialAPI(BASE_URL, USERNAME, PASSWORD);
    tAPI = new TargetAPI(BASE_URL, USERNAME, PASSWORD);

    File csv = new File(INPUT_CSV_FILE_PATH);
    List<String> lines = FlexFileUtils.read(csv);

    TargetGroupThread tg = new TargetGroupThread(tAPI, TARGET_GROUP_CODE);
    CredentialStoreThread cs = new CredentialStoreThread(credAPI);
    tg.start();
    cs.start();
    tg.join();

    if (tg.exception != null)
    {
      throw new Exception(tg.exception.getMessage());
    }

    ReaderValidatorThread readerValidatorThread = new ReaderValidatorThread(tAPI, pAPI, envAPI, TARGET_GROUP_CODE, tg.targetGroupId, WORKFLOW_NAME, lines);
    readerValidatorThread.start();
    readerValidatorThread.join();
    if (readerValidatorThread.exception != null)
    {
      throw new Exception(readerValidatorThread.exception.getMessage());
    }

    PropertyThread propertyThread = new PropertyThread(pAPI, readerValidatorThread.incomingPropertyKeyDefinitions, readerValidatorThread.propertySetObject);
    propertyThread.start();
    cs.join();
    if (cs.exception != null)
    {
      throw new Exception(cs.exception.getMessage());
    }

    CredentialThread credentialThread = new CredentialThread(credAPI, cs.localCredStoreId, cs.localCredStoreInputDefId, readerValidatorThread.credentialNameToValue);
    credentialThread.start();
    
    TargetValueThread targetValueThread = new TargetValueThread(tAPI, TARGET_GROUP_CODE, tg.targetGroupId, readerValidatorThread.targetEnvironmentCodes, readerValidatorThread.codeToValue,
                                                                readerValidatorThread.credentialNameToValue, readerValidatorThread.environmentCodeToEnvironmentId);
    credentialThread.start();
    credentialThread.join();
    if (credentialThread.exception != null)
    {
      throw new Exception(credentialThread.exception.getMessage());
    }

    targetValueThread.credentialNameToId = credentialThread.credentialNameToId;
    targetValueThread.join();
    if (targetValueThread.exception != null)
    {
      throw new Exception(targetValueThread.exception.getMessage());
    }
  }

  private static void validate()
    throws Exception
  {
    final String methodName = "validate";
    LOGGER.entering(CLZ_NAM, methodName);

    if (FlexCommonUtils.isEmpty(BASE_URL) || FlexCommonUtils.isEmpty(USERNAME) || FlexCommonUtils.isEmpty(PASSWORD) || FlexCommonUtils.isEmpty(WORKFLOW_NAME)
        || FlexCommonUtils.isEmpty(TARGET_GROUP_CODE) || FlexCommonUtils.isEmpty(INPUT_CSV_FILE_PATH))
    {
      throw new RuntimeException("BASE_URL, USERNAME, PASSWORD, WORKFLOW_NAME, TARGET_GROUP_CODE and INPUT_CSV_FILE_PATH cannot be empty");
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
