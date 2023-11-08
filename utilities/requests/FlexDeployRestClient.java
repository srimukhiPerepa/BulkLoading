package utilites.requests;

import flexagon.ff.common.core.exceptions.FlexCheckedException;
import flexagon.ff.common.core.logging.FlexLogger;
import flexagon.ff.common.core.rest.FlexRESTClient;
import flexagon.ff.common.core.rest.FlexRESTClientResponse;
import flexagon.ff.common.core.utils.FlexCommonUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FlexDeployRestClient
{
  private static final String CLZ_NAM = FlexDeployRestClient.class.getName();
  private static final FlexLogger LOG = FlexLogger.getLogger(CLZ_NAM);

  static
  {
    System.setProperty("https.protocols", "TLSv1.2");
  }

  private final String mBaseUrl;
  private final String mQueryParamsString;
  private String mAuthHeader;

  public FlexDeployRestClient(String pBaseUrl, String pUsername, String pPassword)
    throws FlexCheckedException
  {
    super();
    mBaseUrl = pBaseUrl.endsWith("/") ? pBaseUrl.substring(0, pBaseUrl.lastIndexOf("/")) : pBaseUrl;
    initAuthHeader(pUsername, pPassword);
  }

  private void initAuthHeader(String pUsername, String pPassword)
    throws FlexCheckedException
  {
    final String methodName = "initAuthHeader";
    LOG.logInfoEntering(methodName);

    String encoded = Base64.getEncoder().encodeToString(String.format("%s:%s", pUsername, pPassword).getBytes());
    mAuthHeader = String.format("Basic %s", encoded);

    LOG.logInfoExiting(methodName);
  }

  public FlexRESTClientResponse get(Request pRequest)
    throws FlexCheckedException
  {
    final String methodName = "get";
    LOG.logFineEntering(methodName);

    FlexRESTClient rest = getFlexRestUtil(pRequest);
    FlexRESTClientResponse response = rest.get();

    validateResponse(response);

    LOG.logFineExiting(methodName);

    return response;
  }

  public FlexRESTClientResponse post(Request pRequest)
    throws FlexCheckedException
  {
    final String methodName = "post";
    LOG.logFineEntering(methodName);

    FlexRESTClient rest = getFlexRestUtil(pRequest);
    FlexRESTClientResponse response = rest.post(pRequest.getBody());

    validateResponse(response);

    LOG.logFineExiting(methodName);

    return response;
  }

  public FlexRESTClientResponse put(Request pRequest)
    throws FlexCheckedException
  {
    final String methodName = "put";
    LOG.logFineEntering(methodName);

    FlexRESTClient rest = getFlexRestUtil(pRequest);
    FlexRESTClientResponse response = rest.put(pRequest.getBody());

    validateResponse(response);

    LOG.logFineExiting(methodName);

    return response;
  }

  public FlexRESTClientResponse delete(Request pRequest)
    throws FlexCheckedException
  {
    final String methodName = "delete";
    LOG.logFineEntering(methodName);

    FlexRESTClient rest = getFlexRestUtil(pRequest);
    FlexRESTClientResponse response = rest.delete();

    validateResponse(response);

    LOG.logFineExiting(methodName);

    return response;
  }

  protected void validateResponse(FlexRESTClientResponse pResponse)
    throws FlexCheckedException
  {
    final String methodName = "validateResponse";
    LOG.logFinestEntering(methodName);

    int responseCode = pResponse.getResponseCode();

    LOG.logInfo(methodName, "Http request returned {0} ", responseCode);

    // All other errors should provide a message
    if (responseCode < 400)
    {
      //if our good(less than 400) status code has an override then throw
      if (mOverrideCodes.contains(responseCode))
      {
        throw new FlexCheckedException("HTTP_REQUEST_FAIL", "Request failed due to overridden status code: " + pResponse.getResponseString());
      }
    }

    LOG.logFinestExiting(methodName);
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
