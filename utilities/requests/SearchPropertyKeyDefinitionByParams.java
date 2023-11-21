package requests;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import javax.ws.rs.client.Entity;

public class SearchPropertyKeyDefinitionByParams
  extends Request
{
  Map<String, Object> mParams = new HashMap<>();

  public SearchPropertyKeyDefinitionByParams()
  {
    super();
  }

  public void setLimit(int pLimit)
  {
    this.mLimit = pLimit;
  }

  public void setOffset(int pOffset)
  {
    this.mOffset = pOffset;
  }

  public void setQueryParams(Map<String, Object> pParams)
  {
    this.mParams = pParams;
  }

  @Override
  public Map<String, Object> getQueryParams()
  {
    return mParams;
  }

  @Override
  public String getResourceUri()
  {
    return "flexdeploy/rest/v2/administration/propertykeydefinition";
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
