package requests;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class CreatePropertyKeyDefinition
  extends Request
{
  private String mJson = null;

  public CreatePropertyKeyDefinition()
  {
    super();
  }

  public void setJson(String pJson)
  {
    this.mJson = pJson;
  }

  @Override
  public Map<String, Object> getQueryParams()
  {
    return Collections.emptyMap();
  }

  @Override
  public String getResourceUri()
  {
    return "flexdeploy/rest/v2/administration/propertykeydefinition";
  }

  @Override
  public Entity getBody()
  {
    return Entity.entity(mJson, MediaType.APPLICATION_JSON);
  }

  @Override
  public Map<String, Object> getHeaders()
  {
    // TODO Implement this method
    return Collections.emptyMap();
  }
}
