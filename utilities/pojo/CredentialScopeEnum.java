package pojo;

public enum CredentialScopeEnum
{
  ENDPOINT("Endpoint"),
  PROPERTY("Property");
  
  private final String mDisplayName;

  CredentialScopeEnum(String name)
  {
    mDisplayName = name;
  }

  public String getDisplayName()
  {
    return mDisplayName;
  }
}