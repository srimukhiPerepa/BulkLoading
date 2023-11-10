package requests;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import javax.ws.rs.client.Entity;

public class GetCredentialStoreByName
  extends Request
{
  private String mName;

  public GetCredentialStoreByName()
  {
    super();
  }

  public void setName(String pName)
  {
    this.mName = pName;
  }

  @Override
  public Map<String, Object> getQueryParams()
  {
    HashMap<String, Object> params = new HashMap<>();
    params.put("credentialStoreName", encodeValue(mName));
    return params;
  }

  @Override
  public String getResourceUri()
  {
    return "flexdeploy/rest/v2/administration/security/credentialstore";
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
