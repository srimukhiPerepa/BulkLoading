package threads;

import requests.CredentialAPI;

import flexagon.ff.common.core.exceptions.FlexCheckedException;

import pojo.CredentialScopeEnum;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.*;

import java.io.*;

/**
 * Credential Thread
 */
public class CThread extends Thread
{
  private final String CLZ_NAM = CThread.class.getName();
  private final Logger LOGGER = Logger.getGlobal();

  // in
  private CredentialAPI credAPI;
  private Map<String, String> credentialNameToValue; //key is credentialName_targetGroupCode_environmentCode, value is credential value
  private String localCredStoreId;
  private String localCredStoreInputDefId;

  // out
  public Map<String, String> credentialNameToId = new HashMap<>();

  public CThread(CredentialAPI credAPI, Map<String, String> credentialNameToValue, String localCredStoreId, String localCredStoreInputDefId)
  {
    this.credAPI = credAPI;
    this.credentialNameToValue = credentialNameToValue;
    this.localCredStoreId = localCredStoreId;
    this.localCredStoreInputDefId = localCredStoreInputDefId;
  }

  public void run()
  {
    try
    {
      for (String credentialName: credentialNameToValue.keySet())
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
    }
    catch (FlexCheckedException fce)
    {
      throw new RuntimeException(fce);
    }
  }

  /**
   * Validates pJsonArray contains zero or more than one JSONObject(s) and return the JSONObject or null
   * pJsonArray - Array of JSONObject containing Credentials
   */
  private JSONObject validateCredentialArray(String pCredentialName, JSONArray pJsonArray)
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