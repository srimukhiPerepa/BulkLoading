package xxt.oracle.apps.ak.xxperson.schema.server;
import oracle.apps.fnd.framework.server.OAEntityImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.domain.Number;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.RowID;
import oracle.jbo.AttributeList;
//  ---------------------------------------------------------------
//  ---    File generated by Oracle Business Components for Java. RK
//  ---------------------------------------------------------------
// Change 1/29/2021 11:19 AM
// Change 1/29/2021 11:29 AM
// Change 1/29/2021 12:27 AM
//ticket 2270
// Change 3/1/2021 4:19 PM 1
// Change 2
// Change 4
// Change 5
// Change 6 7 8 9 10 11 12 13 14 15 16 17 18 19

public class xxPersonDetailsEOImpl extends OAEntityImpl 
{
  protected static final int PERSONID = 0;
  protected static final int FIRSTNAME = 1;
  protected static final int LASTNAME = 2;
  protected static final int EMAIL = 3;
  protected static final int STUDENTFLAG = 4;
  protected static final int NAMEOFUNIVERSITY = 5;
  protected static final int LASTUPDATEDATE = 6;
  protected static final int LASTUPDATELOGIN = 7;
  protected static final int LASTUPDATEDBY = 8;
  protected static final int CREATIONDATE = 9;
  protected static final int CREATEDBY = 10;
  protected static final int ROWID = 11;


  private static oracle.apps.fnd.framework.server.OAEntityDefImpl mDefinitionObject;

  /**
   * 
   * This is the default constructor (do not remove)
   */
  public xxPersonDetailsEOImpl()
  {
  }

  //comment
  //comment 2
  //comment 3
  
  /**
   * 
   * Retrieves the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject()
  {
    if (mDefinitionObject == null)
    {
      mDefinitionObject = (oracle.apps.fnd.framework.server.OAEntityDefImpl)EntityDefImpl.findDefObject("xxt.oracle.apps.ak.xxperson.schema.server.xxPersonDetailsEO");
    }
    return mDefinitionObject;
  }



  /**
   * 
   * Add Entity validation code in this method.
   */
  protected void validateEntity()
  {
    super.validateEntity();
  }

  /**
   * 
   * Gets the attribute value for PersonId, using the alias name PersonId
   */
  public Number getPersonId()
  {
    return (Number)getAttributeInternal(PERSONID);
  }

  /**
   * 
   * Sets <code>value</code> as the attribute value for PersonId
   */
  public void setPersonId(Number value)
  {
    setAttributeInternal(PERSONID, value);
  }

  /**
   * 
   * Gets the attribute value for FirstName, using the alias name FirstName
   */
  public String getFirstName()
  {
    return (String)getAttributeInternal(FIRSTNAME);
  }

  /**
   * 
   * Sets <code>value</code> as the attribute value for FirstName
   */
  public void setFirstName(String value)
  {
    setAttributeInternal(FIRSTNAME, value);
  }

  /**
   * 
   * Gets the attribute value for LastName, using the alias name LastName
   */
  public String getLastName()
  {
    return (String)getAttributeInternal(LASTNAME);
  }

  /**
   * 
   * Sets <code>value</code> as the attribute value for LastName
   */
  public void setLastName(String value)
  {
    setAttributeInternal(LASTNAME, value);
  }

  /**
   * 
   * Gets the attribute value for Email, using the alias name Email
   */
  public String getEmail()
  {
    return (String)getAttributeInternal(EMAIL);
  }

  /**
   * 
   * Sets <code>value</code> as the attribute value for Email
   */
  public void setEmail(String value)
  {
    setAttributeInternal(EMAIL, value);
  }

  /**
   * 
   * Gets the attribute value for StudentFlag, using the alias name StudentFlag
   */
  public String getStudentFlag()
  {
    return (String)getAttributeInternal(STUDENTFLAG);
  }

  /**
   * 
   * Sets <code>value</code> as the attribute value for StudentFlag
   */
  public void setStudentFlag(String value)
  {
    setAttributeInternal(STUDENTFLAG, value);
  }

  /**
   * 
   * Gets the attribute value for NameOfUniversity, using the alias name NameOfUniversity
   */
  public String getNameOfUniversity()
  {
    return (String)getAttributeInternal(NAMEOFUNIVERSITY);
  }

  /**
   * 
   * Sets <code>value</code> as the attribute value for NameOfUniversity
   */
  public void setNameOfUniversity(String value)
  {
    setAttributeInternal(NAMEOFUNIVERSITY, value);
  }

  /**
   * 
   * Gets the attribute value for LastUpdateDate, using the alias name LastUpdateDate
   */
  public Date getLastUpdateDate()
  {
    return (Date)getAttributeInternal(LASTUPDATEDATE);
  }

  /**
   * 
   * Sets <code>value</code> as the attribute value for LastUpdateDate
   */
  public void setLastUpdateDate(Date value)
  {
    setAttributeInternal(LASTUPDATEDATE, value);
  }

  /**
   * 
   * Gets the attribute value for LastUpdateLogin, using the alias name LastUpdateLogin
   */
  public Number getLastUpdateLogin()
  {
    return (Number)getAttributeInternal(LASTUPDATELOGIN);
  }

  /**
   * 
   * Sets <code>value</code> as the attribute value for LastUpdateLogin
   */
  public void setLastUpdateLogin(Number value)
  {
    setAttributeInternal(LASTUPDATELOGIN, value);
  }

  /**
   * 
   * Gets the attribute value for LastUpdatedBy, using the alias name LastUpdatedBy
   */
  public Number getLastUpdatedBy()
  {
    return (Number)getAttributeInternal(LASTUPDATEDBY);
  }

  /**
   * 
   * Sets <code>value</code> as the attribute value for LastUpdatedBy
   */
  public void setLastUpdatedBy(Number value)
  {
    setAttributeInternal(LASTUPDATEDBY, value);
  }

  /**
   * 
   * Gets the attribute value for CreationDate, using the alias name CreationDate
   */
  public Date getCreationDate()
  {
    return (Date)getAttributeInternal(CREATIONDATE);
  }

  /**
   * 
   * Sets <code>value</code> as the attribute value for CreationDate
   */
  public void setCreationDate(Date value)
  {
    setAttributeInternal(CREATIONDATE, value);
  }

  /**
   * 
   * Gets the attribute value for CreatedBy, using the alias name CreatedBy
   */
  public Number getCreatedBy()
  {
    return (Number)getAttributeInternal(CREATEDBY);
  }

  /**
   * 
   * Sets <code>value</code> as the attribute value for CreatedBy
   */
  public void setCreatedBy(Number value)
  {
    setAttributeInternal(CREATEDBY, value);
  }

  /**
   * 
   * Gets the attribute value for RowID, using the alias name RowID
   */
  public RowID getRowID()
  {
    return (RowID)getAttributeInternal(ROWID);
  }
  //  Generated method. Do not modify.

  protected Object getAttrInvokeAccessor(int index, AttributeDefImpl attrDef) throws Exception
  {
    switch (index)
      {
      case PERSONID:
        return getPersonId();
      case FIRSTNAME:
        return getFirstName();
      case LASTNAME:
        return getLastName();
      case EMAIL:
        return getEmail();
      case STUDENTFLAG:
        return getStudentFlag();
      case NAMEOFUNIVERSITY:
        return getNameOfUniversity();
      case LASTUPDATEDATE:
        return getLastUpdateDate();
      case LASTUPDATELOGIN:
        return getLastUpdateLogin();
      case LASTUPDATEDBY:
        return getLastUpdatedBy();
      case CREATIONDATE:
        return getCreationDate();
      case CREATEDBY:
        return getCreatedBy();
      case ROWID:
        return getRowID();
      default:
        return super.getAttrInvokeAccessor(index, attrDef);
      }
  }
  //  Generated method. Do not modify.

  protected void setAttrInvokeAccessor(int index, Object value, AttributeDefImpl attrDef) throws Exception
  {
    switch (index)
      {
      case PERSONID:
        setPersonId((Number)value);
        return;
      case FIRSTNAME:
        setFirstName((String)value);
        return;
      case LASTNAME:
        setLastName((String)value);
        return;
      case EMAIL:
        setEmail((String)value);
        return;
      case STUDENTFLAG:
        setStudentFlag((String)value);
        return;
      case NAMEOFUNIVERSITY:
        setNameOfUniversity((String)value);
        return;
      case LASTUPDATEDATE:
        setLastUpdateDate((Date)value);
        return;
      case LASTUPDATELOGIN:
        setLastUpdateLogin((Number)value);
        return;
      case LASTUPDATEDBY:
        setLastUpdatedBy((Number)value);
        return;
      case CREATIONDATE:
        setCreationDate((Date)value);
        return;
      case CREATEDBY:
        setCreatedBy((Number)value);
        return;
      default:
        super.setAttrInvokeAccessor(index, value, attrDef);
        return;
      }
  }

  /**
   * 
   * Add attribute defaulting logic in this method.
   */
  public void create(AttributeList attributeList)
  {
    super.create(attributeList);
    setPersonId(getOADBTransaction().getSequenceValue("PER_PEOPLE_S"));
  }
}
