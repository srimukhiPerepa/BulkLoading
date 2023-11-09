package requests;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.client.Entity;

public class SearchWorkflowByName
  extends Request
{
  private String mName;

  public SearchWorkflowByName()
  {
    super();
  }

  public String getWorkflowName()
  {
    return mName;
  }

  public void setWorkflowName(String pName)
  {
    this.mName = pName;
  }

  @Override
  public Map<String, Object> getQueryParams()
  {
    HashMap<String, Object> params = new HashMap<>();
    params.put("workflowName", getWorkflowName());
    return params;
  }

  @Override
  public String getResourceUri()
  {
    return String.format("%s/%s", "flexdeploy/rest/v1/workflows", mName);
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
