package requests;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class UpdateTargetGroupById
  extends Request
{
  private String mJson = null;
  private String mId;

  public UpdateTargetGroupById()
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
    return String.format("%s/%s", "flexdeploy/rest/v2/topology/targetgroup", mId);
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
