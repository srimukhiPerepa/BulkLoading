package requests;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import javax.ws.rs.client.Entity;

public class GetEnvironmentByCode
  extends Request
{
  private String mCode;

  public GetEnvironmentByCode()
  {
    super();
  }

  public void setCode(String pCode)
  {
    this.mCode = pCode;
  }

  @Override
  public Map<String, Object> getQueryParams()
  {
    HashMap<String, Object> params = new HashMap<>();
    params.put("environmentCode", mCode);
    return params;
  }

  @Override
  public String getResourceUri()
  {
    return "flexdeploy/rest/v1/topology/environment";
  }

  @Override
  public Entity getBody()
  {
    return null;
  }

  @Override
  public Map<String, Object> getHeaders()
  {
    // TODO Implement this method
    return Collections.emptyMap();
  }
}
