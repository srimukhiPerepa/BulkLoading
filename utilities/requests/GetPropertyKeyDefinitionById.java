package requests;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.client.Entity;

public class GetPropertyKeyDefinitionById
  extends Request
{
  private String mId;

  public GetPropertyKeyDefinitionById()
  {
    super();
  }

  public String getId()
  {
    return mId;
  }

  public void setId(String pId)
  {
    this.mId = pId;
  }

  @Override
  public Map<String, Object> getQueryParams()
  {
    return Collections.emptyMap();
  }

  @Override
  public String getResourceUri()
  {
    return String.format("%s/%s", "flexdeploy/rest/v2/administration/propertykeydefinition", mId);
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
