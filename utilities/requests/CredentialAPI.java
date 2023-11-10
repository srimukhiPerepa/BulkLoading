package requests;

import requests.SearchCredentialByName;
import requests.PatchCredentialById;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.rest.FlexRESTClientResponse;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.logging.*;

public class CredentialAPI
  extends BaseAPI
{
  private static final String CLZ_NAM = WorkflowAPI.class.getName();
  private static final Logger LOGGER = Logger.getGlobal();

  public CredentialAPI(String pBaseUrl, String pUsername, String pPassword)
  {
    super(pBaseUrl, pUsername, pPassword);
  }

  public JSONArray findCredentialByName(String pCredentialName)
    throws FlexCheckedException
  {
    final String methodName = "findCredentialByName";
    LOGGER.entering(CLZ_NAM, methodName, pCredentialName);

    SearchCredentialByName sc = new SearchCredentialByName();
    sc.setName(pCredentialName);
    FlexRESTClientResponse response = getClient().get(sc);

    JSONArray jsonArray = new JSONArray(response.getResponseObject(String.class));
    
    LOGGER.exiting(CLZ_NAM, methodName, jsonArray);
    return jsonArray;
  }

  public void patchCredentialById(String pCredentialId, String pJSONRequestBody)
    throws FlexCheckedException
  {
    final String methodName = "patchCredentialById";
    LOGGER.entering(CLZ_NAM, methodName, pCredentialId);

    PatchCredentialById pc = new PatchCredentialById();
    pc.setId(pWorkflowId);
    pc.setJson(pJSONRequestBody);
    FlexRESTClientResponse response = client.put(pc);

    LOGGER.exiting(CLZ_NAM, methodName);
  }
}