package pojo;

import java.util.List;

public class CredentialPojo
{
  private Long mCredentialId;
  private String mCredentialName;
  private CredentialScopeEnum mCredentialScope;
  private Long mCredentialStoreId;
  private Boolean mIsActive;
  private String mTestConnectionType;
  private Long mTestPropertyKeyDefId1;
  private Long mTestPropertyKeyDefId2;
  private Long mTestPropertyKeyDefId3;
  private Long mTestPropertyKeyDefId4;
  private Long mTestPropertyKeyDefId5;
  private String mTestValue1;
  private String mTestValue2;
  private String mTestValue3;
  private String mTestValue4;
  private String mTestValue5;
  private Timestamp mCreatedOn;
  private String mCreatedBy;
  private Timestamp mUpdatedOn;
  private String mUpdatedBy;
  private Integer mVersionNumber;
  private List<CredentialInputDataObject> mCredentialInputs;

  public CredentialPojo()
  {
    super();
  }

  public Long getCredentialId()
  {
    return this.mCredentialId;
  }

  public void setCredentialId(Long pCredentialId)
  {
    this.mCredentialId = pCredentialId;
  }

  public String getCredentialName()
  {
    return this.mCredentialName;
  }

  public void setCredentialName(String pCredentialName)
  {
    this.mCredentialName = pCredentialName;
  }

  public CredentialScopeEnum getCredentialScope()
  {
    return this.mCredentialScope;
  }

  public void setCredentialScope(CredentialScopeEnum pCredentialScope)
  {
    this.mCredentialScope = pCredentialScope;
  }

  public Long getCredentialStoreId()
  {
    return this.mCredentialStoreId;
  }

  public void setCredentialStoreId(Long pCredentialStoreId)
  {
    this.mCredentialStoreId = pCredentialStoreId;
  }

  public Boolean getIsActive()
  {
    return this.mIsActive;
  }

  public void setIsActive(Boolean pIsActive)
  {
    this.mIsActive = pIsActive;
  }

  public String getTestConnectionType()
  {
    return this.mTestConnectionType;
  }

  public void setTestConnectionType(String pTestConnectionType)
  {
    this.mTestConnectionType = pTestConnectionType;
  }

  public Long getTestPropertyKeyDefId1()
  {
    return this.mTestPropertyKeyDefId1;
  }

  public void setTestPropertyKeyDefId1(Long pTestPropertyKeyDefId1)
  {
    this.mTestPropertyKeyDefId1 = pTestPropertyKeyDefId1;
  }

  public Long getTestPropertyKeyDefId2()
  {
    return this.mTestPropertyKeyDefId2;
  }

  public void setTestPropertyKeyDefId2(Long pTestPropertyKeyDefId2)
  {
    this.mTestPropertyKeyDefId2 = pTestPropertyKeyDefId2;
  }

  public Long getTestPropertyKeyDefId3()
  {
    return this.mTestPropertyKeyDefId3;
  }

  public void setTestPropertyKeyDefId3(Long pTestPropertyKeyDefId3)
  {
    this.mTestPropertyKeyDefId3 = pTestPropertyKeyDefId3;
  }

  public Long getTestPropertyKeyDefId4()
  {
    return this.mTestPropertyKeyDefId4;
  }

  public void setTestPropertyKeyDefId4(Long pTestPropertyKeyDefId4)
  {
    this.mTestPropertyKeyDefId4 = pTestPropertyKeyDefId4;
  }

  public Long getTestPropertyKeyDefId5()
  {
    return this.mTestPropertyKeyDefId5;
  }

  public void setTestPropertyKeyDefId5(Long pTestPropertyKeyDefId5)
  {
    this.mTestPropertyKeyDefId5 = pTestPropertyKeyDefId5;
  }

  public String getTestValue1()
  {
    return this.mTestValue1;
  }

  public void setTestValue1(String pTestValue1)
  {
    this.mTestValue1 = pTestValue1;
  }

  public String getTestValue2()
  {
    return this.mTestValue2;
  }

  public void setTestValue2(String pTestValue2)
  {
    this.mTestValue2 = pTestValue2;
  }

  public String getTestValue3()
  {
    return this.mTestValue3;
  }

  public void setTestValue3(String pTestValue3)
  {
    this.mTestValue3 = pTestValue3;
  }

  public String getTestValue4()
  {
    return this.mTestValue4;
  }

  public void setTestValue4(String pTestValue4)
  {
    this.mTestValue4 = pTestValue4;
  }

  public String getTestValue5()
  {
    return this.mTestValue5;
  }

  public void setTestValue5(String pTestValue5)
  {
    this.mTestValue5 = pTestValue5;
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

  public List<CredentialInputDataObject> getCredentialInputs()
  {
    return this.mCredentialInputs;
  }

  public void setCredentialInputs(List<CredentialInputDataObject> pCredentialInputs)
  {
    this.mCredentialInputs = pCredentialInputs;
  }
}