package requests;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public class PatchTargetById
  extends Request
{
  private String mJson = null;
  private String mEnvironmentId;
  private String mTargetGroupId;

  public PatchTargetById()
  {
    super();
  }

  public void setJson(String pJson)
  {
    this.mJson = pJson;
  }

  public void setEnvironmentId(String pId)
  {
    this.mEnvironmentId = pId;
  }

  public void setTargetGroupId(String pId)
  {
    this.mTargetGroupId = pId;
  }

  @Override
  public Map<String, Object> getQueryParams()
  {
    return Collections.emptyMap();
  }

  @Override
  public String getResourceUri()
  {
    return String.format("%s/%s/%s", "flexdeploy/rest/v1/topology/environmentinstance", mEnvironmentId, mTargetGroupId);
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
