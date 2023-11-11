package threads;

import requests.CredentialAPI;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;
import java.util.logging.*;

import java.io.*;

public class CSThread extends Thread
{
  private final String CLZ_NAM = CSThread.class.getName();
  private final Logger LOGGER = Logger.getGlobal();

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