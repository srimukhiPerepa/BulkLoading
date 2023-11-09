package requests;

import workflow.BulkWorkflowPropertiesAndValues;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.logging.FlexLogger;
import flexagon.ff.common.core.rest.FlexRESTClient;
import flexagon.ff.common.core.rest.FlexRESTClientResponse;
import flexagon.ff.common.core.utils.FlexCommonUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class FlexDeployRestClient
{
  private static final String CLZ_NAM = FlexDeployRestClient.class.getName();
  private static final Logger LOGGER = Logger.getGlobal();

  static
  {
    System.setProperty("https.protocols", "TLSv1.2");
  }

  private final String mBaseUrl;
  private final String mQueryParamsString = "";
  private String mAuthHeader;

  public FlexDeployRestClient(String pBaseUrl, String pUsername, String pPassword)
    throws FlexCheckedException
  {
    mBaseUrl = pBaseUrl.endsWith("/") ? pBaseUrl.substring(0, pBaseUrl.lastIndexOf("/")) : pBaseUrl;
    initAuthHeader(pUsername, pPassword);
  }

  private void initAuthHeader(String pUsername, String pPassword)
    throws FlexCheckedException
  {
    final String methodName = "initAuthHeader";
    LOGGER.entering(CLZ_NAM, methodName);

    String encoded = Base64.getEncoder().encodeToString(String.format("%s:%s", pUsername, pPassword).getBytes());
    mAuthHeader = String.format("Basic %s", encoded);

    LOGGER.exiting(CLZ_NAM, methodName);
  }

  public FlexRESTClientResponse get(Request pRequest)
    throws FlexCheckedException
  {
    final String methodName = "get";
    LOGGER.entering(CLZ_NAM, methodName);

    FlexRESTClient rest = getFlexRestUtil(pRequest);
    FlexRESTClientResponse response = rest.get();

    validateResponse(response);

    LOGGER.exiting(CLZ_NAM, methodName);
    return response;
  }

  public FlexRESTClientResponse post(Request pRequest)
    throws FlexCheckedException
  {
    final String methodName = "post";
    LOGGER.entering(CLZ_NAM, methodName);

    FlexRESTClient rest = getFlexRestUtil(pRequest);
    FlexRESTClientResponse response = rest.post(pRequest.getBody());

    validateResponse(response);

    LOGGER.exiting(CLZ_NAM, methodName);
    return response;
  }

  public FlexRESTClientResponse put(Request pRequest)
    throws FlexCheckedException
  {
    final String methodName = "put";
    LOGGER.entering(CLZ_NAM, methodName);

    FlexRESTClient rest = getFlexRestUtil(pRequest);
    FlexRESTClientResponse response = rest.put(pRequest.getBody());

    validateResponse(response);

    LOGGER.exiting(CLZ_NAM, methodName);
    return response;
  }

  public FlexRESTClientResponse delete(Request pRequest)
    throws FlexCheckedException
  {
    final String methodName = "delete";
    LOGGER.entering(CLZ_NAM, methodName);

    FlexRESTClient rest = getFlexRestUtil(pRequest);
    FlexRESTClientResponse response = rest.delete();

    validateResponse(response);

    LOGGER.exiting(CLZ_NAM, methodName);
    return response;
  }

  protected void validateResponse(FlexRESTClientResponse pResponse)
    throws FlexCheckedException
  {
    final String methodName = "validateResponse";
    LOGGER.entering(CLZ_NAM, methodName);

    int responseCode = pResponse.getResponseCode();
    LOGGER.info("Http request returned " + responseCode);

    // All other errors should provide a message
    if (responseCode >= 400)
    {
      throw new FlexCheckedException("HTTP_REQUEST_FAIL", "Request failed due to: " + pResponse.getResponseString());
    }

    LOGGER.exiting(CLZ_NAM, methodName);
  }

  protected static String getUri(String pBaseUri, String pResourceUri)
  {
    if (!pResourceUri.startsWith("/"))
    {
      pResourceUri = String.format("/%s", pResourceUri);
    }
    return pBaseUri + pResourceUri;
  }

  private FlexRESTClient getFlexRestUtil(Request pRequest)
    throws FlexCheckedException
  {
    String uri = getUri(mBaseUrl, pRequest.getResourceUri());
    Map<String, Object> qParamsMap = pRequest.getQueryParams();

    FlexRESTClient restService = new FlexRESTClient();
    restService.url(uri).queryparameters(qParamsMap).headers(pRequest.getHeaders());
    if (FlexCommonUtils.isNotEmpty(mQueryParamsString))
    {
      // set new instance of map as query param since most of the rest request methods set an empty map
      if (FlexCommonUtils.isEmpty(qParamsMap))
      {
        restService.queryparameters(new HashMap<>());
      }
      restService.queryparameters(mQueryParamsString);
    }
    restService.addHeader("Authorization", mAuthHeader);

    return restService;
  }

}
