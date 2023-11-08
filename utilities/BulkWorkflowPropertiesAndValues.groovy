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

  public static void main(String[] args)
  {
    FlexRESTClient restService = getClient()
    FlexRESTClientResponse response = restService.get();
            
    JSONObject jsonResponse = FlexJsonUtils.getJSON(response.getResponseString());
    println jsonResponse.toString(2)
  }

  private static FlexRESTClient getClient()
  {
    FlexRESTClient restService = new FlexRESTClient();
    restService.url("http://fdtlt86.flexagon.azure.com:8000").path("flexdeploy/rest/v1/workflows/27700536").basicauth("jayar", "Welcome1!").mediatype(MediaType.APPLICATION_JSON).setValidateResponse(true);
    return restService;
  }
}
