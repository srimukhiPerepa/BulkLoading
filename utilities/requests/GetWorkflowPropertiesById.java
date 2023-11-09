package requests;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.client.Entity;

public class GetWorkflowPropertiesById
  extends Request
{
  private String mId;

  public GetWorkflowPropertiesById()
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
    return String.format("flexdeploy/rest/v1/workflows/%s/properties", mId);
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
