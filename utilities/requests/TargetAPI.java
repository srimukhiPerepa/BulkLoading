package requests;

import requests.GetTargetGroupByCode;
import requests.UpdateTargetGroupById;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.rest.FlexRESTClientResponse;
import flexagon.ff.common.core.utils.FlexJsonUtils;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.logging.*;

public class TargetAPI
  extends BaseAPI
{
  private static final String CLZ_NAM = TargetAPI.class.getName();
  private static final Logger LOGGER = Logger.getGlobal();

  public TargetAPI(String pBaseUrl, String pUsername, String pPassword)
    throws FlexCheckedException
  {
    super(pBaseUrl, pUsername, pPassword);
  }

  public JSONArray findTargetGroupByCode(String pTargetGroupCode)
    throws FlexCheckedException
  {
    final String methodName = "findTargetGroupByCode";
    LOGGER.entering(CLZ_NAM, methodName, pTargetGroupCode);

    GetTargetGroupByCode gt = new GetTargetGroupByCode();
    gt.setCode(pTargetGroupCode);

    JSONObject jsonObject = FlexJsonUtils.getJSON(getClient().get(gt));
    JSONArray items = jsonObject.getJSONArray("items");
    
    LOGGER.exiting(CLZ_NAM, methodName, items);
    return items;
  }

  public void updateTargetGroupById(String pTargetGroupId, String pJSONRequestBody)
    throws FlexCheckedException
  {
    final String methodName = "updateTargetGroupById";
    LOGGER.entering(CLZ_NAM, methodName, new Object[]{pTargetGroupId, pJSONRequestBody});

    UpdateTargetGroupById ut = new UpdateTargetGroupById();
    ut.setId(pWorkflowId);
    ut.setJson(pJSONRequestBody);
    FlexRESTClientResponse response = getClient().put(ut);

    LOGGER.exiting(CLZ_NAM, methodName);
  }
}