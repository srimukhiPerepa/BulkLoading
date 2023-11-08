import java.util.Collections;
import java.util.Map;

import javax.ws.rs.client.Entity;

public class GetTargetGroupById
  extends Request
{
  private String mId;

  public GetTargetGroupById()
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
    return String.format("%s/%s", "flexdeploy/rest/v1/topology/targetgroup/", mId);
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
