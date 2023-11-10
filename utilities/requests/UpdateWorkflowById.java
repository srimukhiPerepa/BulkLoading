package requests;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class UpdateWorkflowById
  extends Request
{
  private String mJson = null;
  private String mId;

  public UpdateWorkflowById()
  {
    super();
  }

  public void setJson(String pJson)
  {
    this.mJson = pJson;
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
    return String.format("%s/%s", "flexdeploy/rest/v1/workflows", mId);
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
