package workflow;

import requests.FlexDeployRestClient;
import requests.GetTargetGroupByCode;
import requests.SearchWorkflowByName;

import pojo.PropertyDefinitionPojo;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.logging.FlexLogger;
import flexagon.ff.common.core.rest.FlexRESTClient;
import flexagon.ff.common.core.rest.FlexRESTClientResponse;
import flexagon.ff.common.core.utils.FlexJsonUtils;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.*;
 
public class BulkWorkflowPropertiesAndValues
{
  private static final String CLZ_NAM = BulkWorkflowPropertiesAndValues.class.getName();
  private static final FlexLogger LOG = FlexLogger.getLogger(CLZ_NAM);
  private static final Logger LOGGER = Logger.getGlobal();

  protected static String BASE_URL;
  protected static String USERNAME;
  protected static String PASSWORD;
  protected static String WORKFLOW_NAME;
  protected static String TARGET_GROUP_CODE;

  private static FlexDeployRestClient client;

  public static void main(String[] args)
    throws FlexCheckedException
  {
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		LOGGER.addHandler(consoleHandler);
		LOGGER.setLevel(Level.ALL);
		LOGGER.setUseParentHandlers(false);

    if (args == null || args.length < 5)
    {
      throw new IllegalArgumentException("BASE_URL, USERNAME, PASSWORD, WORKFLOW_NAME, and TARGET_GROUP_CODE must be passed as arguments.");
    }
    BASE_URL = args[0];
    USERNAME = args[1];
    PASSWORD = args[2];
    WORKFLOW_NAME = args[3];
    TARGET_GROUP_CODE = args[4];

    client = new FlexDeployRestClient(BASE_URL, USERNAME, PASSWORD);
    String workflowId = findWorkflow();
    List<PropertyDefinitionPojo> workflowProperties = getWorkflowProperties(workflowId);

    // GetTargetGroupByCode tg = new GetTargetGroupByCode();
    // tg.setCode(TARGET_GROUP_CODE);
    // FlexRESTClientResponse response = client.get(tg);
  }

  private static List<PropertyDefinitionPojo> getWorkflowProperties(String pWorkflowId)
  {
    final String methodName = "getWorkflowProperties";
    LOGGER.entering(CLZ_NAM, methodName, pWorkflowId);

    List<PropertyDefinitionPojo> results = new ArrayList<>();
    GetWorkflowPropertiesById wp = new GetWorkflowPropertiesById();
    wp.setId(workflowId);
    FlexRESTClientResponse response = client.get(wp);

    String jsonString = response.getResponseObject(String.class);
    LOGGER.info("Workflow properties response: " + jsonString);

    JSONArray jsonArray = new JSONArray(jsonString);
    for (int i = 0; i < jsonArray.length(); i++)
    {
      JSONObject object = jsonArray.getJSONObject(i);
      PropertyDefinitionPojo propertyDef = new PropertyDefinitionPojo();
      boolean isEncrypted = object.getBoolean("isEncrypted");
      String dataType = object.getString("dataType");
      Object displayRows = object.get("displayRows");
      Object displayColumns = object.get("displayColumns");
      Object listData = object.get("listData");
      boolean isRequired = object.getBoolean("isRequired");
      Object subDataType = object.get("subDataType");
      boolean isDefaultValueExpression = object.getBoolean("isDefaultValueExpression");
      boolean isMultiselect = object.getBoolean("isMultiselect");
      Object displayName = object.get("displayName");
      Object description = object.get("description");
      String scope = object.getString("scope");
      boolean isActive = object.getBoolean("isActive");
      Object defaultValue = object.get("defaultValue");
      String name = object.getString("name");

      propertyDef.setIsEncrypted(isEncrypted);
      propertyDef.setIsRequired(isRequired);
      propertyDef.setIsDefaultValueExpression(isDefaultValueExpression);
      propertyDef.setIsMultiselect(isMultiselect);
      propertyDef.setIsActive(isActive);
      propertyDef.setDataType(dataType);
      propertyDef.setScope(scope);
      propertyDef.setCode(name);

      if (displayRows != null)
      {
        propertyDef.setDisplayRows(Integer.parseInt(displayRows.toString()));
      }

      if (displayColumns != null)
      {
        propertyDef.setDisplayColumns(Integer.parseInt(displayColumns.toString()));
      }

      if (listData != null)
      {
        propertyDef.setListData(Arrays.asList(listData.trim().split(",")));
      }

      if (subDataType != null)
      {
        propertyDef.setSubDataType(subDataType.toString());
      }

      if (displayName != null)
      {
        propertyDef.setDisplayName(displayName.toString());
      }

      if (description != null)
      {
        propertyDef.setDescription(description.toString());
      }

      if (defaultValue != null)
      {
        propertyDef.setDefaultValue(defaultValue.toString());
      }

      results.add(propertyDef);
    }

    LOGGER.exiting(CLZ_NAM, methodName);
  }

  private static String findWorkflowId()
    throws FlexCheckedException
  {
    final String methodName = "findWorkflow";
    LOGGER.entering(CLZ_NAM, methodName);

    SearchWorkflowByName sw = new SearchWorkflowByName();
    sw.setWorkflowName(WORKFLOW_NAME);
    FlexRESTClientResponse response = client.get(sw);

    String jsonString = response.getResponseObject(String.class);
    LOGGER.info("Workflow response: " + jsonString);

    JSONArray jsonArray = new JSONArray(jsonString);
    if (jsonArray.length() == 0)
    {
      throw new FlexCheckedException("No workflow(s) found with name " + WORKFLOW_NAME);
    }

    if (jsonArray.length() > 1)
    {
      throw new FlexCheckedException("More than one workflow found with name " + WORKFLOW_NAME + ". WORKFLOW_NAME must be unique.");
    }

    JSONObject wfObject = jsonArray.getJSONObject(0);
    String workflowId = wfObject.get("workflowId").toString();

    LOGGER.exiting(CLZ_NAM, methodName, workflowId);
    return workflowId;
  }
}
