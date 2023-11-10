package pojo;

import flexagon.ff.common.core.utils.FlexCommonUtils;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;
import java.util.ArrayList;

public class PropertyDefinitionPojo implements Cloneable
{
  private String mName; //Property code
  private String mDisplayName;
  private String mScope;
  private String mDescription;
  private String mDataType;
  private String mSubDataType;
  private String mDefaultValue;
  private Boolean mIsMultiselect;
  private Boolean mIsDefaultValueExpression;
  private Boolean mIsRequired;
  private Boolean mIsEncrypted;
  private Boolean mIsActive;
  private List<String> mListData;
  private Integer mDisplayRows;
  private Integer mDisplayColumns;

  @Override
  protected Object clone() throws CloneNotSupportedException 
  {
    PropertyDefinitionPojo clone = (PropertyDefinitionPojo) super.clone();
    clone.setListData(new ArrayList<>(mListData));
    return clone;
  } 

  public PropertyDefinitionPojo()
  {
    super();
  }

  public PropertyDefinitionPojo(String pName)
  {
    super();
    mName = pName;
  }

  public void setName(String pName)
  {
    this.mName = pName;
  }

  public String getName()
  {
    return mName;
  }

  public void setDisplayName(String pDisplayName)
  {
    this.mDisplayName = pDisplayName;
  }

  public String getDisplayName()
  {
    return mDisplayName;
  }

  public void setScope(String pScope)
  {
    this.mScope = pScope;
  }

  public String getScope()
  {
    return mScope;
  }

  public void setDescription(String pDescription)
  {
    this.mDescription = pDescription;
  }

  public String getDescription()
  {
    return mDescription;
  }

  public void setDataType(String pDataType)
  {
    this.mDataType = pDataType;
  }

  public String getDataType()
  {
    return mDataType;
  }

  public void setSubDataType(String pSubDataType)
  {
    this.mSubDataType = pSubDataType;
  }

  public String getSubDataType()
  {
    return mSubDataType;
  }

  public void setDefaultValue(String pDefaultValue)
  {
    this.mDefaultValue = pDefaultValue;
  }

  public String getDefaultValue()
  {
    return mDefaultValue;
  }

  public void setIsMultiselect(Boolean pIsMultiselect)
  {
    this.mIsMultiselect = pIsMultiselect;
  }

  public Boolean getIsMultiselect()
  {
    return mIsMultiselect;
  }

  public void setIsDefaultValueExpression(Boolean pIsDefaultValueExpression)
  {
    this.mIsDefaultValueExpression = pIsDefaultValueExpression;
  }

  public Boolean getIsDefaultValueExpression()
  {
    return mIsDefaultValueExpression;
  }

  public void setIsRequired(Boolean pIsRequired)
  {
    this.mIsRequired = pIsRequired;
  }

  public Boolean getIsRequired()
  {
    return mIsRequired;
  }

  public void setIsEncrypted(Boolean pIsEncrypted)
  {
    this.mIsEncrypted = pIsEncrypted;
  }

  public Boolean getIsEncrypted()
  {
    return mIsEncrypted;
  }

  public void setIsActive(Boolean pIsActive)
  {
    this.mIsActive = pIsActive;
  }

  public Boolean getIsActive()
  {
    return mIsActive;
  }

  public void setListData(List<String> pListData)
  {
    this.mListData = pListData;
  }

  public List<String> getListData()
  {
    return mListData;
  }

  public void setDisplayRows(Integer pRows)
  {
    this.mDisplayRows = pRows;
  }

  public Integer getDisplayRows()
  {
    return mDisplayRows;
  }

  public void setDisplayColumns(Integer pCols)
  {
    this.mDisplayColumns = pCols;
  }

  public Integer getDisplayColumns()
  {
    return mDisplayColumns;
  }

  public JSONObject toJson()
  {
    JSONObject object = new JSONObject();
    // required
    object.put("isEncrypted", mIsEncrypted);
    object.put("dataType", mDataType);
    object.put("isRequired", mIsRequired);
    object.put("scope", mScope);
    object.put("name", mName);

    // optional
    object.put("displayRows", mDisplayRows);
    object.put("displayColumns", mDisplayColumns);
    object.put("listData", FlexCommonUtils.isEmpty(mListData) ? null : FlexCommonUtils.toString(mListData, ",", false));
    object.put("isActive", mIsActive == null ? true : mIsActive); // default is true
    object.put("subDataType", FlexCommonUtils.isEmpty(mSubDataType) ? null : mSubDataType);
    object.put("isDefaultValueExpression", mIsDefaultValueExpression == null ? false : mIsDefaultValueExpression); // default is false
    object.put("isMultiselect", mIsMultiselect == null ? false : mIsMultiselect); // default is false
    object.put("displayName", mDisplayName);
    object.put("defaultValue", mDefaultValue);
    object.put("description", mDescription);

    return object;
  }

  @Override
  public boolean equals(Object o) {
      if (o == this)
          return true;
      if (!(o instanceof PropertyDefinitionPojo))
          return false;
      PropertyDefinitionPojo other = (PropertyDefinitionPojo) o;
      return this.mName.equals(other.mName);
  }

  @Override
  public final int hashCode() {
      int result = 17;
      if (mName != null) {
          result = 31 * result + mName.hashCode();
      }
      return result;
  }
}
