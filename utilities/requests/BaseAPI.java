package requests;

public abstract class BaseAPI
{
    private FlexDeployRestClient client;

    public BaseAPI(String pBaseUrl, String pUsername, String pPassword)
    {
        client = new FlexDeployRestClient(pBaseUrl, pUsername, pPassword);
    }

    protected FlexDeployRestClient getClient()
    {
        return client;
    }
}