package requests;

import flexagon.ff.common.core.logging.FlexLogger;

import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.net.URLDecoder;

public abstract class Request
{
  private static final String CLZ_NAM = Request.class.getName();
  private static Logger logger;

  public Request()
  {
    super();
    logger = BulkWorkflowPropertiesAndValues.logger;
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

  public String encodeValue(String value) 
  {
    try 
    {
      return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
    } 
    catch(UnsupportedEncodingException uee)
    {
      throw new RuntimeException(uee);
    }
  }

  public String decodeValue(String value)
  {
    try 
    {
      return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
    } 
    catch(UnsupportedEncodingException uee)
    {
      throw new RuntimeException(uee);
    }
  }
}