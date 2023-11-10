package workflow;

import requests.FlexDeployRestClient;
import requests.GetTargetGroupByCode;
import requests.SearchWorkflowByName;
import requests.UpdateWorkflowById;

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
  protected static String WORKFLOW_SOURCE;

  private static FlexDeployRestClient client;
  private static Map<String, String> codeToValue = new HashMap<>(); //key is code##environment_code, value is target property value

  public static void main(String[] args)
    throws FlexCheckedException
  {
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		LOGGER.addHandler(consoleHandler);
		LOGGER.setLevel(Level.ALL);
		LOGGER.setUseParentHandlers(false);

    if (args == null || args.length < 6)
    {
      throw new IllegalArgumentException("BASE_URL, USERNAME, PASSWORD, WORKFLOW_NAME, TARGET_GROUP_CODE, and WORKFLOW_SOURCE must be passed as arguments.");
    }
    BASE_URL = args[0];
    USERNAME = args[1];
    PASSWORD = args[2];
    WORKFLOW_NAME = args[3];
    TARGET_GROUP_CODE = args[4];
    WORKFLOW_SOURCE = args[5];

    client = new FlexDeployRestClient(BASE_URL, USERNAME, PASSWORD);
    JSONObject workflowObject = findWorkflow();
    workflowObject.put("sourceCode", WORKFLOW_SOURCE);
    workflowObject.put("sourceCodeURL", "dummy"); // this is required or validation will fail - sourceCodeURL is not actually used in backend
    String workflowId = workflowObject.get("workflowId").toString();
    List<PropertyDefinitionPojo> existingWorkflowProperties = parseWorkflowProperties(workflowObject.getJSONArray("properties"));
    File csv = new File("../examples/workflow_property_values.csv");
    List<String> lines = FlexFileUtils.read(csv);
    List<PropertyDefinitionPojo> updatedWorkflowProperties = processCSV(lines);

    LOGGER.fine("codeToValue mapping: " + codeToValue);

    LOGGER.fine("Merging existing workflow properties with incoming properties from " + csv);
    // merge both lists with updatedWorkflowProperties taking precedence if there are duplicates
    List<PropertyDefinitionPojo> mergedWorkflowProperties = new ArrayList<>(existingWorkflowProperties);
    for (int i = 0; i < updatedWorkflowProperties.size(); i++)
    {
      PropertyDefinitionPojo pojo = updatedWorkflowProperties.get(i);
      int index = mergedWorkflowProperties.indexOf(pojo);
      if (index != -1)
      {
        LOGGER.info("Workflow Property with code " + pojo.getName() + " already exists in the workflow. Overriding values.");
        mergedWorkflowProperties.set(index, pojo);
      }
      else
      {
        LOGGER.info("Adding new Workflow Property with code " + pojo.getName());
        mergedWorkflowProperties.add(pojo);
      }
    }

    // Write merged results to workflowObject
    JSONArray workflowPropertiesArray = new JSONArray();
    for (PropertyDefinitionPojo pojo : mergedWorkflowProperties)
    {
      workflowPropertiesArray.put(pojo.toJson());
    }
    workflowObject.put("properties", workflowPropertiesArray);

    LOGGER.info("Final Workflow Object: " + workflowObject.toString(2));

    UpdateWorkflowById uw = new UpdateWorkflowById();
    uw.setId(workflowId);
    uw.setJson(workflowObject.toString());
    FlexRESTClientResponse response = client.put(uw);

    // GetTargetGroupByCode tg = new GetTargetGroupByCode();
    // tg.setCode(TARGET_GROUP_CODE);
    // FlexRESTClientResponse response = client.get(tg);
  }

  private static List<PropertyDefinitionPojo> processCSV(List<String> pLines)
    throws FlexCheckedException
  {
    final String methodName = "processCSV";
    LOGGER.entering(CLZ_NAM, methodName, pLines);

    List<PropertyDefinitionPojo> results = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    String[] headers = pLines.get(0).split(",");
    List<String> environmentCodes = new ArrayList<>();
    for (int i = 15; i < headers.length; i++)
    {
      environmentCodes.add(headers[i]);
    }
    int numEnvironments = environmentCodes.size();

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

      PropertyDefinitionPojo pojo = new PropertyDefinitionPojo();
      pojo.setIsEncrypted(Boolean.valueOf(isEncrypted));
      pojo.setIsRequired(Boolean.valueOf(isRequired));
      pojo.setIsDefaultValueExpression(isDefaultValueExpression != null ? Boolean.valueOf(isDefaultValueExpression) : false);
      pojo.setIsMultiselect(isMultiselect != null ? Boolean.valueOf(isMultiselect) : false);
      pojo.setIsActive(isActive != null ? Boolean.valueOf(isActive) : true);
      pojo.setDataType(dataType);
      pojo.setScope(propertyScope);
      pojo.setName(code);
      pojo.setDisplayRows(FlexCommonUtils.isNotEmpty(displayRows) ? Integer.parseInt(displayRows.toString()) : null);
      pojo.setDisplayColumns(FlexCommonUtils.isNotEmpty(displayColumns) ? Integer.parseInt(displayColumns.toString()) : null);
      pojo.setListData(FlexCommonUtils.isNotEmpty(listData) ? Arrays.asList(listData.toString().trim().split(",")) : null);
      pojo.setSubDataType(FlexCommonUtils.isNotEmpty(subDataType) ? subDataType.toString() : null);
      pojo.setDisplayName(FlexCommonUtils.isNotEmpty(displayName) ? displayName.toString() : null);
      pojo.setDescription(FlexCommonUtils.isNotEmpty(description) ? description.toString() : null);
      pojo.setDefaultValue(FlexCommonUtils.isNotEmpty(defaultValue) ? defaultValue.toString() : null);

      results.add(pojo);

      for (int j = 0; j < numEnvironments; j++)
      {
        String key = code + environmentCodes.get(j);
        String value = tokens[j+15]; //important to add 15 here
        codeToValue.put(key, value);
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

      if (FlexCommonUtils.isNotEmpty(subDataType) && !(subDataType.equals("DIRECTORY") || subDataType.equals("JDBCURL") || subDataType.equals("URL")))
      {
        errors.add("Line " + i + " SUB_DATA_TYPE must be DIRECTORY, JDBCURL or URL");
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

    LOGGER.exiting(CLZ_NAM, methodName, results.size());
    return results;
  }

  private static List<PropertyDefinitionPojo> parseWorkflowProperties(JSONArray pJsonArr)
    throws FlexCheckedException
  {
    final String methodName = "parseWorkflowProperties";
    LOGGER.entering(CLZ_NAM, methodName, pJsonArr);

    List<PropertyDefinitionPojo> results = new ArrayList<>();
    for (int i = 0; i < pJsonArr.length(); i++)
    {
      JSONObject object = pJsonArr.getJSONObject(i);
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
      propertyDef.setDisplayRows(FlexCommonUtils.isNotEmpty(displayRows.toString()) && !"null".equals(displayRows.toString()) ? Integer.parseInt(displayRows.toString()) : null);
      propertyDef.setDisplayColumns(FlexCommonUtils.isNotEmpty(displayColumns.toString()) && !"null".equals(displayColumns.toString()) ? Integer.parseInt(displayColumns.toString()) : null);
      propertyDef.setListData(FlexCommonUtils.isNotEmpty(listData.toString()) && !"null".equals(listData.toString()) ? Arrays.asList(listData.toString().trim().split(",")) : null);
      propertyDef.setSubDataType(FlexCommonUtils.isNotEmpty(subDataType.toString()) && !"null".equals(subDataType.toString()) ? subDataType.toString() : null);
      propertyDef.setDisplayName(FlexCommonUtils.isNotEmpty(displayName.toString()) && !"null".equals(displayName.toString()) ? displayName.toString() : null);
      propertyDef.setDescription(FlexCommonUtils.isNotEmpty(description.toString()) && !"null".equals(description.toString()) ? description.toString() : null);
      propertyDef.setDefaultValue(FlexCommonUtils.isNotEmpty(defaultValue.toString()) && !"null".equals(defaultValue.toString()) ? defaultValue.toString() : null);

      results.add(propertyDef);
    }

    LOGGER.exiting(CLZ_NAM, methodName, results.size());
    return results;
  }

  private static JSONObject findWorkflow()
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
    
    LOGGER.exiting(CLZ_NAM, methodName, wfObject);
    return wfObject;
  }
}
