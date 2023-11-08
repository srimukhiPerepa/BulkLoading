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
  protected final String BASE_URL = "${{FD_BASE_URL}}";
  protected final String USERNAME = "${{FD_USERNAME}}";
  protected final String PASSWORD = "${{FD_PASSWORD}}";

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
    restService.url(BASE_URL).path("flexdeploy/rest/v1/workflows/27700536").basicauth(USERNAME, PASSWORD).mediatype(MediaType.APPLICATION_JSON).setValidateResponse(true);
    return restService;
  }

  private static getCredentialsFromAccount
}
