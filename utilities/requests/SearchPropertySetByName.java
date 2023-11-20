package requests;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import javax.ws.rs.client.Entity;

public class SearchPropertySetByName
  extends Request
{
  private String mName;

  public SearchPropertySetByName()
  {
    super();
  }

  public void setName(String pName)
  {
    this.mName = pName;
  }

  @Override
  public Map<String, Object> getQueryParams()
  {
    Map<String, Object> params = new HashMap<>();
    params.put("propertyKeyName", mName);
    return params;
  }

  @Override
  public String getResourceUri()
  {
    return "flexdeploy/rest/v2/administration/propertyset";
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
