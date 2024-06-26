package requests;

import requests.GetEnvironmentByCode;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.rest.FlexRESTClientResponse;
import flexagon.ff.common.core.utils.FlexJsonUtils; 

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.logging.*;

public class EnvironmentAPI
  extends BaseAPI
{
  private static final String CLZ_NAM = EnvironmentAPI.class.getName();
  private static final Logger LOGGER = Logger.getGlobal();

  public EnvironmentAPI(String pBaseUrl, String pUsername, String pPassword)
    throws FlexCheckedException
  {
    super(pBaseUrl, pUsername, pPassword);
  }

  public JSONArray findEnvironmentByCode(String pEnvironmentCode)
    throws FlexCheckedException
  {
    final String methodName = "findEnvironmentByCode";
    LOGGER.entering(CLZ_NAM, methodName, pEnvironmentCode);

    GetEnvironmentByCode ge = new GetEnvironmentByCode();
    ge.setCode(pEnvironmentCode);
    FlexRESTClientResponse response = getClient().get(ge);

    JSONArray jsonArray = new JSONArray(response.getResponseObject(String.class));
    
    LOGGER.exiting(CLZ_NAM, methodName, jsonArray);
    return jsonArray;
  }
}