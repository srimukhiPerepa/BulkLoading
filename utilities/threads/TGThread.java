package threads;

import requests.TargetAPI;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;
import java.util.logging.*;

public class TGThread extends Thread
{
  private final String CLZ_NAM = TGThread.class.getName();
  private final Logger LOGGER = Logger.getGlobal();

  // in
  private TargetAPI tAPI;
  private String targetGroupCode;

  // out
  public Exception exception;
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
      exception = fce;
    }

    LOGGER.info(CLZ_NAM + " completed successfully");
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