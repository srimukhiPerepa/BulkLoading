package threads;

import requests.TargetAPI;

import pojo.*;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;
import java.util.logging.*;

import java.io.*;

/**
 * Thread to update target values
 */
public class TargetValueThread extends Thread
{
  private final String CLZ_NAM = TargetValueThread.class.getName();
  private final Logger LOGGER = Logger.getGlobal();

  // in
  private TargetAPI tAPI;
  private String targetGroupCode;
  private String targetGroupId;
  private List<PropertyKeyDefinitionDataObject> propertyKeyDefinitions;
  private List<String> targetEnvironmentCodes;
  private Map<String, String> codeToValue; //key is code+environmentCode, value is target property value
  private Map<String, String> credentialNameToValue; //key is credentialName_targetGroupCode_environmentCode, value is credential value
  private Map<String, String> environmentCodeToEnvironmentId;
  public Map<String, String> credentialNameToId;

  // out
  public Exception exception;

  public TargetValueThread(TargetAPI tAPI, String targetGroupCode, String targetGroupId, List<PropertyKeyDefinitionDataObject> propertyKeyDefinitions,
                          List<String> targetEnvironmentCodes, Map<String, String> codeToValue, 
                          Map<String, String> credentialNameToValue, Map<String, String> environmentCodeToEnvironmentId)
  {
    this.tAPI = tAPI;
    this.targetGroupCode = targetGroupCode;
    this.targetGroupId = targetGroupId;
    this.propertyKeyDefinitions = propertyKeyDefinitions;
    this.targetEnvironmentCodes = targetEnvironmentCodes;
    this.codeToValue = codeToValue;
    this.credentialNameToValue = credentialNameToValue;
    this.environmentCodeToEnvironmentId = environmentCodeToEnvironmentId;
  }

  public void run()
  {
    try
    {
      int index = 1;
      int total = propertyKeyDefinitions.size() * targetEnvironmentCodes.size();
      for (PropertyKeyDefinitionDataObject prop : propertyKeyDefinitions)
      {
        String propertyKeyName = prop.getPropertyKeyName();
        String scope = prop.getPropertyScope();
        boolean isEncrypted = prop.getIsEncrypted();

        if (!"ENVINST".equals(scope))
        {
          LOGGER.info("Skipping " + scope + " property " + propertyKeyName + " - " + (index++) + " of " + total);
          continue;
        }

        for (String environmentCode : targetEnvironmentCodes)
        {
          String environmentId = environmentCodeToEnvironmentId.get(environmentCode);
          String targetValue = codeToValue.get(propertyKeyName + environmentCode);

          JSONArray propertiesArray = new JSONArray();
          JSONObject property = new JSONObject();
          property.put("propertyName", propertyKeyName);

          if (isEncrypted)
          {
            String credentialName = String.format("%s_%s_%s", propertyKeyName, targetGroupCode, environmentCode);
            while (credentialNameToId == null)
            {
              LOGGER.fine("Waiting 3 seconds for credentialNameToId map to populate...");
              Thread.sleep(3);
            }

            // If credential is not CSV then credentialName will not be a key in credentialNameToId map
            if (!credentialNameToId.containsKey(credentialName))
            {
              continue;
            }
            
            LOGGER.info(String.format("Patching target property %s (credential) to environment %s - %d of %d", propertyKeyName, environmentCode, index++, total));
            property.put("credentialId", credentialNameToId.get(credentialName));
          }
          else 
          {
            LOGGER.info(String.format("Patching target property %s to environment %s - %d of %d", propertyKeyName, environmentCode, index++, total));
            property.put("propertyValue", targetValue);
          }
          propertiesArray.put(property);

          JSONObject patchRequestBody = new JSONObject();
          patchRequestBody.put("properties", propertiesArray);

          tAPI.patchTargetById(environmentId, targetGroupId, patchRequestBody.toString());
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      exception = ex;
    }

    LOGGER.info(CLZ_NAM + " completed successfully");
  }
}