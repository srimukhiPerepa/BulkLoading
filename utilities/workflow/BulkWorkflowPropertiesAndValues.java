package workflow;

import requests.FlexDeployRestClient;
import requests.GetTargetGroupByCode;
import requests.SearchWorkflowByName;
import requests.GetWorkflowPropertiesById;

import pojo.PropertyDefinitionPojo;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.logging.FlexLogger;
import flexagon.ff.common.core.rest.FlexRESTClient;
import flexagon.ff.common.core.rest.FlexRESTClientResponse;
import flexagon.ff.common.core.utils.FlexJsonUtils;
import flexagon.ff.common.core.utils.FlexCommonUtils;
import flexagon.ff.common.core.utils.FlexFileUtils;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.*;

import java.io.File;
 
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
  private static List<String> targetEnvironmentCodes = new ArrayList<>(); //In order
  private static Map<String, List<String>> codeToValue = new HashMap<>(); //key is workflow property code value is List<String> or value for each target property in order

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
    String workflowId = findWorkflowId();
    List<PropertyDefinitionPojo> workflowProperties = getWorkflowProperties(workflowId);
    List<String> lines = FlexFileUtils.read(new File("../examples/workflow_property_values.csv"));
    processCSV(lines);

    // GetTargetGroupByCode tg = new GetTargetGroupByCode();
    // tg.setCode(TARGET_GROUP_CODE);
    // FlexRESTClientResponse response = client.get(tg);
  }

  private static void processCSV(List<String> pLines)
    throws FlexCheckedException
  {
    final String methodName = "processCSV";
    LOGGER.entering(CLZ_NAM, methodName, pLines);

    List<String> errors = new ArrayList<>();

    String[] headers = pLines.get(0).split(",");
    int numEnvironments = 0;
    for (int i = 15; i < headers.length; i++)
    {
      numEnvironments++;
      targetEnvironmentCodes.add(headers[i]);
    }

    int numLines = pLines.size();
    for (int i = 1; i < numLines; i++)
    {
      String line = pLines.get(i);
      String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
      String code = tokens[0];
      String displayName = tokens[1];
      String propertyScope = tokens[2];
      String isRequired = tokens[3];
      String dataType = tokens[4];
      String isEncrypted = tokens[5];
      String displayRows = tokens[6];
      String displayColumns = tokens[7];
      String listData = tokens[8];
      String subDataType = tokens[9];
      String isDefaultValueExpression = tokens[10];
      String isMultiselect = tokens[11];
      String description = tokens[12];
      String isActive = tokens[13];
      String defaultValue = tokens[14];

      for (int j = 15; j < numEnvironments + 15; j++)
      {
        List<String> valuesForTarget = codeToValue.getOrDefault(code, new ArrayList<>());
        valuesForTarget.add(tokens[j]);
        codeToValue.put(code, valuesForTarget);
      }

      if (FlexCommonUtils.isEmpty(code))
      {
        errors.add("Line " + i + " is missing CODE");
      }

      if (FlexCommonUtils.isEmpty(displayName))
      {
        errors.add("Line " + i + " is missing DISPLAY_NAME");
      }

      if (FlexCommonUtils.isEmpty(propertyScope) || !(propertyScope.equals("TARGET") || propertyScope.equals("PROJECT")))
      {
        errors.add("Line " + i + " PROPERTY_SCOPE must be TARGET or PROJECT");
      }

      if (FlexCommonUtils.isEmpty(isRequired))
      {
        errors.add("Line " + i + " is missing REQUIRED");
      }

      if (FlexCommonUtils.isEmpty(dataType))
      {
        errors.add("Line " + i + " is missing DATA_TYPE");
      }

      if (FlexCommonUtils.isEmpty(isEncrypted))
      {
        errors.add("Line " + i + " is missing ENCRYPTED");
      }
    }

    if (errors.size() > 0)
    {
      throw new FlexCheckedException(errors.toString());
    }

    LOGGER.exiting(CLZ_NAM, methodName);
  }

  private static List<PropertyDefinitionPojo> getWorkflowProperties(String pWorkflowId)
    throws FlexCheckedException
  {
    final String methodName = "getWorkflowProperties";
    LOGGER.entering(CLZ_NAM, methodName, pWorkflowId);

    List<PropertyDefinitionPojo> results = new ArrayList<>();
    GetWorkflowPropertiesById wp = new GetWorkflowPropertiesById();
    wp.setId(pWorkflowId);
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
      propertyDef.setName(name);

      if (displayRows != null && !displayRows.toString().equals("null"))
      {
        propertyDef.setDisplayRows(Integer.parseInt(displayRows.toString()));
      }

      if (displayColumns != null && !displayColumns.toString().equals("null"))
      {
        propertyDef.setDisplayColumns(Integer.parseInt(displayColumns.toString()));
      }

      if (listData != null && !listData.toString().equals("null"))
      {
        propertyDef.setListData(Arrays.asList(listData.toString().trim().split(",")));
      }

      if (subDataType != null && !subDataType.toString().equals("null"))
      {
        propertyDef.setSubDataType(subDataType.toString());
      }

      if (displayName != null && !displayName.toString().equals("null"))
      {
        propertyDef.setDisplayName(displayName.toString());
      }

      if (description != null && !description.toString().equals("null"))
      {
        propertyDef.setDescription(description.toString());
      }

      if (defaultValue != null && !defaultValue.toString().equals("null"))
      {
        propertyDef.setDefaultValue(defaultValue.toString());
      }

      results.add(propertyDef);
    }

    LOGGER.exiting(CLZ_NAM, methodName, results.size());
    return results;
  }

  private static String findWorkflowId()
    throws FlexCheckedException
  {
    final String methodName = "findWorkflowId";
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
