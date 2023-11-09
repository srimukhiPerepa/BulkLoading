package workflow;

import requests.FlexDeployRestClient;
import requests.GetTargetGroupByCode;
import requests.SearchWorkflowByName;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.rest.FlexRESTClient;
import flexagon.ff.common.core.rest.FlexRESTClientResponse;
import flexagon.ff.common.core.utils.FlexJsonUtils;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
 
public class BulkWorkflowPropertiesAndValues
{
  private static final String CLZ_NAM = BulkWorkflowPropertiesAndValues.class.getName();
  public static final Logger logger = Logger.getGlobal();

  protected static String BASE_URL;
  protected static String USERNAME;
  protected static String PASSWORD;
  protected static String WORKFLOW_NAME;
  protected static String TARGET_GROUP_CODE;

  private static FlexDeployRestClient client = getClient();

  public static void main(String[] args)
    throws FlexCheckedException
  {
    if (args == null || args.length < 5)
    {
      throw new IllegalArgumentException("BASE_URL, USERNAME, PASSWORD, WORKFLOW_NAME, and TARGET_GROUP_CODE must be passed as arguments.");
    }
    BASE_URL = args[0];
    USERNAME = args[1];
    PASSWORD = args[2];
    WORKFLOW_NAME = args[3];
    TARGET_GROUP_CODE = args[4];

    logger.setLevel(Level.ALL);
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		logger.addHandler(consoleHandler);
		logger.setLevel(Level.ALL);
		logger.setUseParentHandlers(false);

    JSONObject workflowObject = findWorkflow();

    // GetTargetGroupByCode tg = new GetTargetGroupByCode();
    // tg.setCode(TARGET_GROUP_CODE);
    // FlexRESTClientResponse response = client.get(tg);

    //JSONObject jsonResponse = FlexJsonUtils.getJSON(response.getResponseString());
  }

  private static FlexDeployRestClient getClient()
    throws FlexCheckedException
  {
    FlexDeployRestClient restService = new FlexDeployRestClient(BASE_URL, USERNAME, PASSWORD);
    return restService;
  }

  private static JSONObject findWorkflow()
  {
    final String methodName = "findWorkflow";
    logger.entering(CLZ_NAM, methodName);

    SearchWorkflowByName sw = new SearchWorkflowByName();
    sw.setWorkflowName(WORKFLOW_NAME);
    FlexRESTClientResponse response = client.get(sw);

    String jsonString = response.getResponseObject(String.class);
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
    System.out.println(wfObject.toString(2));
    String workflowId = wfObject.get("workflowId").toString();

    logger.exiting(CLZ_NAM, methodName, wfObject);
    return wfObject;
  }
}
