package threads;

import requests.PropertyAPI;

import pojo.PropertyKeyDefinitionDataObject;
import pojo.PropertySetKeyDefDataObject;
import pojo.PropertyTypeEnum;

import threads.*;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.utils.FlexCommonUtils;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import java.io.*;

/**
 * Thread to:
 * 1. Create/Update Property Key Definition(s)
 * 2. Associate Property Key Definition(s) to a workflow using PropertySet
 */
public class PropertyThread extends Thread
{
  private final String CLZ_NAM = PropertyThread.class.getName();
  private final Logger LOGGER = Logger.getGlobal();

  // in
  private PropertyAPI pAPI;
  private List<PropertyKeyDefinitionDataObject> propertyKeyDefinitions;
  private JSONObject propertySetObject;

  // out
  public Exception exception;

  public PropertyThread(PropertyAPI pAPI, List<PropertyKeyDefinitionDataObject> propertyKeyDefinitions, JSONObject propertySetObject)
  {
    this.pAPI = pAPI;
    this.propertyKeyDefinitions = propertyKeyDefinitions;
    this.propertySetObject = propertySetObject;
  }

  public void run()
  {
    try
    {
      String propertySetId = propertySetObject.get("propertySetId").toString();
      Long propertySetIdLong = Long.valueOf(propertySetId);

      // merge propertySetKeyDefs with incoming
      JSONArray propertySetKeyDefs = propertySetObject.getJSONArray("propertySetKeyDefs");
      List<JSONObject> converted = IntStream.range(0, propertySetKeyDefs.length())
                                  .mapToObj(i -> propertySetKeyDefs.getJSONObject(i))
                                  .collect(Collectors.toList());
      List<PropertySetKeyDefDataObject> allPropertySetKeyDefs = converted.stream()
                                  .map(json -> {
                                    Long propSetId = Long.valueOf(json.get("propertySetId").toString());
                                    Long propertyDefinitionId = Long.valueOf(json.get("propertyDefinitionId").toString());
                                    return new PropertySetKeyDefDataObject(propSetId, propertyDefinitionId);
                                  })
                                  .collect(Collectors.toList());


      LOGGER.info("Iterating on incoming property key definitions...");
      int index = 1;
      // This iterates on property key definitions from CSV file
      for (PropertyKeyDefinitionDataObject propKeyDef : propertyKeyDefinitions)
      {
        String propertyKeyName = propKeyDef.getPropertyKeyName();
        JSONArray searchResult = pAPI.findPropertyKeyDefinitionByName(propertyKeyName);

        LOGGER.info("Creating/updating property key definition " + propertyKeyName + " " + (index++) + " of " + propertyKeyDefinitions.size());
        if (searchResult.length() == 0)
        {
          // create
          JSONObject requestBody = propKeyDef.toJson();
          JSONObject response = pAPI.createPropertyKeyDefinition(requestBody.toString());
          // add propertyDefinitionId to propKeyDef object
          propKeyDef.setPropertyDefinitionId(response.getLong("propertyDefinitionId"));
        }
        else
        {
          // patch
          String propertyKeyDefinitionId = searchResult.getJSONObject(0).get("propertyDefinitionId").toString();
          JSONObject requestBody = propKeyDef.toJson();
          pAPI.patchPropertyKeyDefinitionById(propertyKeyDefinitionId, requestBody.toString());
          propKeyDef.setPropertyDefinitionId(Long.valueOf("propertyKeyDefinitionId"));
        }

        PropertySetKeyDefDataObject propertySetKeyDef = new PropertySetKeyDefDataObject(propertySetIdLong, propKeyDef.getPropertyDefinitionId());
        if (!allPropertySetKeyDefs.contains(propertySetKeyDef))
        {
          allPropertySetKeyDefs.add(propertySetKeyDef);
        }
      }

      LOGGER.info("Updating Property Set for " + propertySetObject.get("propertySetName"));
      LOGGER.finest("allPropertySetKeyDefs (existing and incoming): " + allPropertySetKeyDefs);

      writeWorkflowPropertySetKeyDefs(propertySetObject, allPropertySetKeyDefs);
      pAPI.updatePropertySetById(propertySetId, propertySetObject.toString());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      exception = ex;
    }

    LOGGER.info(CLZ_NAM + " completed successfully");
  }

  /**
   * Write properties to PropertySet JsonObject
   */
  private void writeWorkflowPropertySetKeyDefs(JSONObject propertySetObject, List<PropertySetKeyDefDataObject> properties)
  {
    final String methodName = "writeWorkflowPropertySetKeyDefs";
    LOGGER.entering(CLZ_NAM, methodName);

    propertySetObject.put("propertySetKeyDefs", new JSONArray()); // clears existing properties
    for (PropertySetKeyDefDataObject pojo : properties)
    {
      propertySetObject.getJSONArray("propertySetKeyDefs").put(pojo.toJson());
    }
    LOGGER.info("Final Workflow Property Set Object: " + propertySetObject.toString(2));

    LOGGER.exiting(CLZ_NAM, methodName);
  }
}