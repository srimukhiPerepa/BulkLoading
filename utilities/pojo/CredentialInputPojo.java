package pojo;

import java.sql.Timestamp;

public class CredentialInputPojo
{
  private Long mCredentialInputId;
  private Long mCredentialId;
  private Long mCredentialStoreInputDefId;
  private String mInputValue;
  private boolean mIsEncrypted;
  private Timestamp mCreatedOn;
  private String mCreatedBy;
  private Timestamp mUpdatedOn;
  private String mUpdatedBy;
  private Integer mVersionNumber;

  public CredentialInputPojo()
  {
    super();
  }

  public Long getCredentialInputId()
  {
    return this.mCredentialInputId;
  }

  public void setCredentialInputId(Long pCredentialInputId)
  {
    this.mCredentialInputId = pCredentialInputId;
  }

  public Long getCredentialId()
  {
    return this.mCredentialId;
  }

  public void setCredentialId(Long pCredentialId)
  {
    this.mCredentialId = pCredentialId;
  }

  public Long getCredentialStoreInputDefId()
  {
    return this.mCredentialStoreInputDefId;
  }

  public void setCredentialStoreInputDefId(Long pCredentialStoreInputDefId)
  {
    this.mCredentialStoreInputDefId = pCredentialStoreInputDefId;
  }

  public String getInputValue()
  {
    return this.mInputValue;
  }

  public void setInputValue(String pInputValue)
  {
    this.mInputValue = pInputValue;
  }

  public boolean getIsEncrypted()
  {
    return this.mIsEncrypted;
  }

  public void setIsEncrypted(boolean pIsEncrypted)
  {
    this.mIsEncrypted = pIsEncrypted;
  }

  public Timestamp getCreatedOn()
  {
    return this.mCreatedOn;
  }

  public void setCreatedOn(Timestamp pCreatedOn)
  {
    this.mCreatedOn = pCreatedOn;
  }

  public String getCreatedBy()
  {
    return this.mCreatedBy;
  }

  public void setCreatedBy(String pCreatedBy)
  {
    this.mCreatedBy = pCreatedBy;
  }

  public Timestamp getUpdatedOn()
  {
    return this.mUpdatedOn;
  }

  public void setUpdatedOn(Timestamp pUpdatedOn)
  {
    this.mUpdatedOn = pUpdatedOn;
  }

  public String getUpdatedBy()
  {
    return this.mUpdatedBy;
  }

  public void setUpdatedBy(String pUpdatedBy)
  {
    this.mUpdatedBy = pUpdatedBy;
  }

  public Integer getVersionNumber()
  {
    return this.mVersionNumber;
  }

  public void setVersionNumber(Integer pVersionNumber)
  {
    this.mVersionNumber = pVersionNumber;
  }
}