package workflow;
import requests.FlexDeployRestClient;
import requests.GetTargetGroupById;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.rest.FlexRESTClient;
import flexagon.ff.common.core.rest.FlexRESTClientResponse;
import flexagon.ff.common.core.utils.FlexJsonUtils;
 
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;
 
public class BulkWorkflowPropertiesAndValues
{
  private static final String GET_WORKFLOW_RESOURCE = "flexdeploy/rest/v1/workflows/27700536";

  protected static String BASE_URL;
  protected static String USERNAME;
  protected static String PASSWORD;

  public static void main(String[] args)
    throws FlexCheckedException
  {
    if (args == null || args.length < 3)
    {
      throw new IllegalArgumentException("BASE_URL, USERNAME, and PASSWORD must be passed as arguments.");
    }
    BASE_URL = args[0];
    USERNAME = args[1];
    PASSWORD = args[2];

    FlexDeployRestClient client = getClient();

    GetTargetGroupById tg = new GetTargetGroupById();
    tg.setId("268056");

    FlexRESTClientResponse response = client.get(tg);
            
    JSONObject jsonResponse = FlexJsonUtils.getJSON(response.getResponseString());
    System.out.println(jsonResponse.toString(2));
  }

  private static FlexDeployRestClient getClient()
    throws FlexCheckedException
  {
    FlexDeployRestClient restService = new FlexDeployRestClient(BASE_URL, USERNAME, PASSWORD);
    return restService;
  }
}
