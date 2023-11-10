package requests;

import requests.SearchCredentialByName;
import requests.PatchCredentialById;
import requests.GetCredentialStoreByName;
import requests.GetCredentialStoreProviderByName;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.rest.FlexRESTClientResponse;
import flexagon.ff.common.core.utils.FlexJsonUtils;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.logging.*;

public class CredentialAPI
  extends BaseAPI
{
  private static final String CLZ_NAM = WorkflowAPI.class.getName();
  private static final Logger LOGGER = Logger.getGlobal();

  public CredentialAPI(String pBaseUrl, String pUsername, String pPassword)
    throws FlexCheckedException
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

    JSONObject jsonObject = FlexJsonUtils.getJSON(getClient().get(sc));
    JSONArray items = jsonObject.getJSONArray("items");
    
    LOGGER.exiting(CLZ_NAM, methodName, items);
    return items;
  }

  public JSONArray getLocalCredentialStore()
    throws FlexCheckedException
  {
    final String methodName = "getLocalCredentialStore";
    LOGGER.entering(CLZ_NAM, methodName);

    GetCredentialStoreByName gcs = new GetCredentialStoreByName();
    gcs.setName("Local");

    JSONObject jsonObject = FlexJsonUtils.getJSON(getClient().get(gcs));
    JSONArray items = jsonObject.getJSONArray("items");
    
    LOGGER.exiting(CLZ_NAM, methodName, items);
    return items;
  }

  public JSONArray getLocalCredentialStoreProvider()
    throws FlexCheckedException
  {
    final String methodName = "getLocalCredentialStoreProvider";
    LOGGER.entering(CLZ_NAM, methodName);

    GetCredentialStoreProviderByName gcsp = new GetCredentialStoreProviderByName();
    gcsp.setName("Local");

    JSONObject jsonObject = FlexJsonUtils.getJSON(getClient().get(gcsp));
    JSONArray items = jsonObject.getJSONArray("items");
    
    LOGGER.exiting(CLZ_NAM, methodName, items);
    return items;
  }

  public JSONObject createCredential(String pJSONRequestBody)
    throws FlexCheckedException
  {
    final String methodName = "createCredential";
    LOGGER.entering(CLZ_NAM, methodName, pJSONRequestBody);

    CreateCredential cc = new CreateCredential();
    cc.setJson(pJSONRequestBody);
    FlexRESTClientResponse response = getClient().put(cc);

    LOGGER.exiting(CLZ_NAM, methodName);
  }

  public void patchCredentialById(String pCredentialId, String pJSONRequestBody)
    throws FlexCheckedException
  {
    final String methodName = "patchCredentialById";
    LOGGER.entering(CLZ_NAM, methodName, new Object[]{pCredentialId, pJSONRequestBody});

    PatchCredentialById pc = new PatchCredentialById();
    pc.setId(pCredentialId);
    pc.setJson(pJSONRequestBody);
    FlexRESTClientResponse response = getClient().patch(pc);

    LOGGER.exiting(CLZ_NAM, methodName);
  }
}