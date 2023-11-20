package pojo;

import flexagon.ff.common.core.utils.FlexCommonUtils;
import flexagon.ff.common.core.exceptions.FlexCheckedException;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import java.util.logging.Logger;

public class PropertyKeyDefinitionDataObject
{
  private static final String CLZ_NAM = PropertyKeyDefinitionDataObject.class.getName();
  private static final Logger LOGGER = Logger.getGlobal();

  private Long mPropertyDefinitionId;
  private String mDefaultValue;
  private String mDescription;
  private Integer mDisplayColumns;
  private String mDisplayName;
  private Integer mDisplayRows;
  private Boolean mIsActive;
  private Boolean mIsAllowsVariant;
  private Boolean mIsDefaultExpression;
  private Boolean mIsEncrypted;
  private Boolean mIsMultiselect;
  private Boolean mIsRequired;
  private Long mLength;
  private String mListData;
  private Long mMaxValue;
  private Long mMinValue;
  private PropertyTypeEnum mPropertyKeyDatatype;
  private String mPropertyKeyName;
  private String mPropertyKeySubDatatype;
  private String mPropertyScope;
  private String mValidator1;

  public PropertyKeyDefinitionDataObject()
  {
    super();
  }

  public Long getPropertyDefinitionId()
  {
    return this.mPropertyDefinitionId;
  }

  public void setPropertyDefinitionId(Long pPropertyDefinitionId)
  {
    this.mPropertyDefinitionId = pPropertyDefinitionId;
  }

  public String getDefaultValue()
  {
    return this.mDefaultValue;
  }

  public void setDefaultValue(String pDefaultValue)
  {
    this.mDefaultValue = pDefaultValue;
  }

  public String getDescription()
  {
    return this.mDescription;
  }

  public void setDescription(String pDescription)
  {
    this.mDescription = pDescription;
  }

  public Integer getDisplayColumns()
  {
    return this.mDisplayColumns;
  }

  public void setDisplayColumns(Integer pDisplayColumns)
  {
    this.mDisplayColumns = pDisplayColumns;
  }

  public String getDisplayName()
  {
    return this.mDisplayName;
  }

  public void setDisplayName(String pDisplayName)
  {
    this.mDisplayName = pDisplayName;
  }

  public Integer getDisplayRows()
  {
    return this.mDisplayRows;
  }

  public void setDisplayRows(Integer pDisplayRows)
  {
    this.mDisplayRows = pDisplayRows;
  }

  public Boolean getIsActive()
  {
    return this.mIsActive;
  }

  public void setIsActive(Boolean pIsActive)
  {
    this.mIsActive = pIsActive;
  }

  public Boolean getIsAllowsVariant()
  {
    return this.mIsAllowsVariant;
  }

  public void setIsAllowsVariant(Boolean pIsAllowsVariant)
  {
    this.mIsAllowsVariant = pIsAllowsVariant;
  }

  public Boolean getIsDefaultExpression()
  {
    return this.mIsDefaultExpression;
  }

  public void setIsDefaultExpression(Boolean pIsDefaultExpression)
  {
    this.mIsDefaultExpression = pIsDefaultExpression;
  }

  public Boolean getIsEncrypted()
  {
    return this.mIsEncrypted;
  }

  public void setIsEncrypted(Boolean pIsEncrypted)
  {
    this.mIsEncrypted = pIsEncrypted;
  }

  public Boolean getIsMultiselect()
  {
    return this.mIsMultiselect;
  }

  public void setIsMultiselect(Boolean pIsMultiselect)
  {
    this.mIsMultiselect = pIsMultiselect;
  }

  public Boolean getIsRequired()
  {
    return this.mIsRequired;
  }

  public void setIsRequired(Boolean pIsRequired)
  {
    this.mIsRequired = pIsRequired;
  }

  public Long getLength()
  {
    return this.mLength;
  }

  public void setLength(Long pLength)
  {
    this.mLength = pLength;
  }

  public String getListData()
  {
    return this.mListData;
  }

  public void setListData(String pListData)
  {
    this.mListData = pListData;
  }

  public Long getMaxValue()
  {
    return this.mMaxValue;
  }

  public void setMaxValue(Long pMaxValue)
  {
    this.mMaxValue = pMaxValue;
  }

  public Long getMinValue()
  {
    return this.mMinValue;
  }

  public void setMinValue(Long pMinValue)
  {
    this.mMinValue = pMinValue;
  }

  public PropertyTypeEnum getPropertyKeyDatatype()
  {
    return this.mPropertyKeyDatatype;
  }

  public void setPropertyKeyDatatype(PropertyTypeEnum pPropertyKeyDatatype)
  {
    this.mPropertyKeyDatatype = pPropertyKeyDatatype;
  }

  public String getPropertyKeyName()
  {
    return this.mPropertyKeyName;
  }

  public void setPropertyKeyName(String pPropertyKeyName)
  {
    this.mPropertyKeyName = pPropertyKeyName;
  }

  public String getPropertyKeySubDatatype()
  {
    return this.mPropertyKeySubDatatype;
  }

  public void setPropertyKeySubDatatype(String pPropertyKeySubDatatype)
  {
    this.mPropertyKeySubDatatype = pPropertyKeySubDatatype;
  }

  public String getPropertyScope()
  {
    return this.mPropertyScope;
  }

  public void setPropertyScope(String pPropertyScope)
  {
    this.mPropertyScope = pPropertyScope;
  }

  public String getValidator1()
  {
    return this.mValidator1;
  }

  public void setValidator1(String pValidator1)
  {
    this.mValidator1 = pValidator1;
  }

  public JSONObject toJson()
  {
    JSONObject object = new JSONObject();
    // required
    object.put("propertyDefinitionId", mPropertyDefinitionId);
    object.put("propertyKeyName", mPropertyKeyName);
    object.put("propertyScope", mPropertyScope);
    object.put("propertyKeyDatatype", mPropertyKeyDatatype);
    object.put("isRequired", mIsRequired);
    object.put("isEncrypted", mIsEncrypted);
    object.put("isActive", mIsActive);
    object.put("isAllowsVariant", mIsAllowsVariant);

    // optional
    object.put("displayName", mDisplayName);
    object.put("description", mDescription);
    object.put("propertyKeySubDatatype", mPropertyKeySubDatatype);
    object.put("minValue", mMinValue);
    object.put("maxValue", mMaxValue);
    object.put("listData", mListData);
    object.put("isMultiselect", mIsMultiselect == null ? false : mIsMultiselect); // default is false
    object.put("displayRows", mDisplayRows);
    object.put("displayColumns", mDisplayColumns);
    object.put("validator1", mValidator1);
    object.put("defaultValue", mDefaultValue);
    object.put("isDefaultExpression", mIsDefaultExpression == null ? false : mIsDefaultExpression); // default is false
    object.put("length", mLength);

    return object;
  }

  public static PropertyKeyDefinitionDataObject fromJson(JSONObject pJson)
    throws FlexCheckedException
  {
    final String methodName = "fromJson";
    LOGGER.entering(CLZ_NAM, methodName, pJson);

    PropertyKeyDefinitionDataObject propertyKeyDef = new PropertyKeyDefinitionDataObject();
    String displayName = pJson.getString("displayName");
    String description = pJson.getString("description");
    String subDataType = pJson.getString("propertyKeySubDatatype");
    Long minValue = pJson.getLong("minValue");
    Long maxValue = pJson.getLong("maxValue");
    String listData = pJson.getString("listData");
    Boolean isMultiselect = pJson.getBoolean("isMultiselect");
    Integer displayRows = pJson.getInt("displayRows");
    Integer displayColumns = pJson.getInt("displayColumns");
    String validator1 = pJson.getString("validator1");
    String defaultValue = pJson.getString("defaultValue");
    Boolean isDefaultExpression = pJson.getBoolean("isDefaultExpression");
    Long length = pJson.getLong("length");

    // required
    propertyKeyDef.setPropertyDefinitionId(pJson.getLong("propertyDefinitionId"));
    propertyKeyDef.setPropertyKeyName(pJson.getString("propertyKeyName"));
    propertyKeyDef.setPropertyScope(pJson.getString("propertyScope"));
    propertyKeyDef.setPropertyKeyDatatype(PropertyTypeEnum.valueOf(pJson.getString("propertyKeyDatatype")));
    propertyKeyDef.setIsRequired(Boolean.valueOf(pJson.getString("isRequired")));
    propertyKeyDef.setIsEncrypted(Boolean.valueOf(pJson.getString("isEncrypted")));
    propertyKeyDef.setIsActive(Boolean.valueOf(pJson.getString("isActive")));
    propertyKeyDef.setIsAllowsVariant(Boolean.valueOf(pJson.getString("isAllowsVariant")));


    // optional
    propertyKeyDef.setDisplayName(displayName);
    propertyKeyDef.setDescription(description);
    propertyKeyDef.setPropertyKeySubDatatype(subDataType);
    propertyKeyDef.setMinValue(minValue);
    propertyKeyDef.setMaxValue(maxValue);
    propertyKeyDef.setListData(listData);
    propertyKeyDef.setIsMultiselect(isMultiselect);
    propertyKeyDef.setDisplayRows(displayRows);
    propertyKeyDef.setDisplayColumns(displayColumns);
    propertyKeyDef.setValidator1(validator1);
    propertyKeyDef.setDefaultValue(defaultValue);
    propertyKeyDef.setIsDefaultExpression(isDefaultExpression);
    propertyKeyDef.setLength(length);

    LOGGER.exiting(CLZ_NAM, methodName, propertyKeyDef);
    return propertyKeyDef;
  }

  public static List<PropertyKeyDefinitionDataObject> convertJSONArrayToObjects(JSONArray pJsonArr)
    throws FlexCheckedException
  {
    final String methodName = "convertJSONArrayToObjects";
    LOGGER.entering(CLZ_NAM, methodName, pJsonArr);

    List<PropertyKeyDefinitionDataObject> results = new ArrayList<>();
    for (int i = 0; i < pJsonArr.length(); i++)
    {
      JSONObject object = pJsonArr.getJSONObject(i);
      PropertyKeyDefinitionDataObject propertyKeyDef = fromJson(object);
      results.add(propertyKeyDef);
    }

    LOGGER.exiting(CLZ_NAM, methodName, results.size());
    return results;
  }

  @Override
  public boolean equals(Object o) 
  {
      if (o == this)
          return true;
      if (!(o instanceof PropertyKeyDefinitionDataObject))
          return false;
      PropertyKeyDefinitionDataObject other = (PropertyKeyDefinitionDataObject) o;
      return Long.compare(this.mPropertyDefinitionId, other.mPropertyDefinitionId) == 0;
  }

  @Override
  public final int hashCode() 
  {
      int result = 17;
      result = 31 * result + mPropertyDefinitionId.hashCode();
      return result;
  }

  @Override
  public String toString() 
  {
    return "PropertyKeyDefinitionDataObject={PropertyDefinitionId=" + mPropertyDefinitionId + ", PropertyKeyName=" + mPropertyKeyName + ", PropertyKeyDatatype=" + mPropertyKeyDatatype + ", PropertyScope=" + mPropertyScope + "}";
  }
}
