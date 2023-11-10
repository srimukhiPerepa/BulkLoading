package requests;

import flexagon.ff.common.core.exceptions.FlexCheckedException;

public abstract class BaseAPI
{
  private FlexDeployRestClient client;

  public BaseAPI(String pBaseUrl, String pUsername, String pPassword) 
    throws FlexCheckedException
  {
    client = new FlexDeployRestClient(pBaseUrl, pUsername, pPassword);
  }

  protected FlexDeployRestClient getClient()
  {
    return client;
  }
}