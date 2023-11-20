package requests;

import requests.SearchPropertySetByName;
import requests.UpdatePropertySetById;
import requests.SearchPropertyKeyDefinitionByName;
import requests.PatchPropertyKeyDefinitionById;
import requests.GetPropertyKeyDefinitionById;
import requests.CreatePropertyKeyDefinition;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.rest.FlexRESTClientResponse;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.logging.*;

public class PropertyAPI
  extends BaseAPI
{
  private static final String CLZ_NAM = PropertyAPI.class.getName();
  private static final Logger LOGGER = Logger.getGlobal();

  public PropertyAPI(String pBaseUrl, String pUsername, String pPassword)
    throws FlexCheckedException
  {
    super(pBaseUrl, pUsername, pPassword);
  }

  public JSONArray findPropertySetByName(String pWorkflowName)
    throws FlexCheckedException
  {
    final String methodName = "findPropertySetByName";
    LOGGER.entering(CLZ_NAM, methodName, pWorkflowName);

    SearchPropertySetByName sps = new SearchPropertySetByName();
    sps.setName(pWorkflowName);
    FlexRESTClientResponse response = getClient().get(sw);

    JSONObject jsonObject = response.getResponseObject(String.class);
    JSONArray items = jsonObject.getJSONArray("items");
    
    LOGGER.exiting(CLZ_NAM, methodName, items);
    return items;
  }

  public JSONArray findPropertyKeyDefinitionByName(String pPropertyKeyName)
    throws FlexCheckedException
  {
    final String methodName = "findPropertyKeyDefinitionByName";
    LOGGER.entering(CLZ_NAM, methodName, pPropertyKeyName);

    SearchPropertyKeyDefinitionByName spkd = new SearchPropertyKeyDefinitionByName();
    spkd.setName(pPropertyKeyName);
    FlexRESTClientResponse response = getClient().get(spkd);

    JSONObject jsonObject = response.getResponseObject(String.class);
    JSONArray items = jsonObject.getJSONArray("items");
    
    LOGGER.exiting(CLZ_NAM, methodName, items);
    return items;
  }

  public void updatePropertySetById(String pPropertySetId, String pJSONRequestBody)
    throws FlexCheckedException
  {
    final String methodName = "updatePropertySetById";
    LOGGER.entering(CLZ_NAM, methodName, new Object[]{pPropertySetId, pJSONRequestBody});

    UpdatePropertySetById ups = new UpdatePropertySetById();
    ups.setId(pPropertySetId);
    ups.setJson(pJSONRequestBody);
    FlexRESTClientResponse response = getClient().put(ups);

    LOGGER.exiting(CLZ_NAM, methodName);
  }

  public void patchPropertyKeyDefinitionById(String pPropertyKeyDefinitionId, String pJSONRequestBody)
    throws FlexCheckedException
  {
    final String methodName = "patchPropertyKeyDefinitionById";
    LOGGER.entering(CLZ_NAM, methodName, new Object[]{pPropertyKeyDefinitionId, pJSONRequestBody});

    PatchPropertyKeyDefinitionById pkd = new PatchPropertyKeyDefinitionById();
    pkd.setId(pPropertyKeyDefinitionId);
    pkd.setJson(pJSONRequestBody);
    FlexRESTClientResponse response = getClient().put(ups);

    LOGGER.exiting(CLZ_NAM, methodName);
  }

  public JSONObject createPropertyKeyDefinition(String pJSONRequestBody)
    throws FlexCheckedException
  {
    final String methodName = "createPropertyKeyDefinition";
    LOGGER.entering(CLZ_NAM, methodName, pJSONRequestBody);

    CreatePropertyKeyDefinition cpkd = new CreatePropertyKeyDefinition();
    cpkd.setJson(pJSONRequestBody);

    JSONObject jsonObject = FlexJsonUtils.getJSON(getClient().post(cpkd));

    LOGGER.exiting(CLZ_NAM, methodName, jsonObject);
    return jsonObject;
  }
}