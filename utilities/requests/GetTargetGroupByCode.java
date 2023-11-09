package requests;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.client.Entity;

public class GetTargetGroupByCode
  extends Request
{
  private String mCode;

  public GetTargetGroupByCode()
  {
    super();
  }

  public String getCode()
  {
    return mCode;
  }

  public void setCode(String pCode)
  {
    this.mCode = pCode;
  }

  @Override
  public Map<String, Object> getQueryParams()
  {
    HashMap<String, Object> params = new HashMap<>();
    params.put("targetGroupCode", getCode())
    return params;
  }

  @Override
  public String getResourceUri()
  {
    return String.format("%s/%s", "flexdeploy/rest/v2/topology/targetgroup", mId);
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
