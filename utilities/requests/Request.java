package requests;

import flexagon.ff.common.core.logging.FlexLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

public abstract class Request
{
  private static final String CLZ_NAM = Request.class.getName();
  private static final FlexLogger LOG = FlexLogger.getLogger(CLZ_NAM);

  public Request()
  {
    super();
  }

  public abstract Map<String, Object> getQueryParams();

  public abstract String getResourceUri();

  /**
   * Only called for post/put/patch requests
   *
   * @return Entity object representing the body
   */
  public abstract Entity getBody();

  public abstract Map<String, Object> getHeaders();

}