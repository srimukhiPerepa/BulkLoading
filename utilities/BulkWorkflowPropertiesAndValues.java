evaluate(new File("./FlexDeployRestClient.groovy"))
evaluate(new File("./Requests/GetTargetGroupById.groovy"))
evaluate(new File("./Requests/Request.groovy"))

import flexagon.ff.common.core.rest.FlexRESTClient;
import flexagon.ff.common.core.rest.FlexRESTClientResponse;
import flexagon.ff.common.core.utils.FlexJsonUtils;
 
import org.json.JSONObject;
import org.json.JSONArray;
  
import java.util.HashMap;
import java.util.Map;
 
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
 
public class BulkWorkflowPropertiesAndValues
{
  private static final GET_WORKFLOW_RESOURCE = "flexdeploy/rest/v1/workflows/27700536";

  protected static String BASE_URL = "${{FD_BASE_URL}}";
  protected static String USERNAME = "${{FD_USERNAME}}";
  protected static String PASSWORD = "${{FD_PASSWORD}}";

  public static void main(String[] args)
  {
    FlexDeployRestClient client = getClient()

    GetTargetGroupById tg = new GetTargetGroupById();
    tg.setId("268056");

    FlexRESTClientResponse response = client.get(tg);
            
    JSONObject jsonResponse = FlexJsonUtils.getJSON(response.getResponseString());
    println jsonResponse.toString(2)
  }

  private static FlexDeployRestClient getClient()
  {
    FlexDeployRestClient restService = new FlexDeployRestClient(BASE_URL, USERNAME, PASSWORD);


    //restService.url(BASE_URL).path(GET_WORKFLOW_RESOURCE).basicauth(USERNAME, PASSWORD).mediatype(MediaType.APPLICATION_JSON).setValidateResponse(true);
    return restService;
  }
}
