package pojo;

import flexagon.ff.common.core.utils.FlexCommonUtils;
import flexagon.ff.common.core.exceptions.FlexCheckedException;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import java.util.logging.Logger;

public class PropertySetKeyDefDataObject
{
  private static final String CLZ_NAM = PropertySetKeyDefDataObject.class.getName();
  private static final Logger LOGGER = Logger.getGlobal();

  private Long mPropertySetId;
  private Long mPropertyDefinitionId;

  public PropertySetKeyDefDataObject()
  {
    super();
  }

  public Long getPropertySetId()
  {
    return this.mPropertySetId;
  }

  public void setPropertySetId(Long pPropertySetId)
  {
    this.mPropertySetId = pPropertySetId;
  }

  public Long getPropertyDefinitionId()
  {
    return this.mPropertyDefinitionId;
  }

  public void setPropertyDefinitionId(Long pPropertyDefinitionId)
  {
    this.mPropertyDefinitionId = pPropertyDefinitionId;
  }

  public JSONObject toJson()
  {
    JSONObject object = new JSONObject();
    object.put("propertyDefinitionId", mPropertyDefinitionId);
    object.put("propertySetId", mPropertySetId);

    return object;
  }

  public static List<PropertySetKeyDefDataObject> convertJSONArrayToObjects(JSONArray pJsonArr)
    throws FlexCheckedException
  {
    final String methodName = "convertJSONArrayToObjects";
    LOGGER.entering(CLZ_NAM, methodName, pJsonArr);

    List<PropertySetKeyDefDataObject> results = new ArrayList<>();
    for (int i = 0; i < pJsonArr.length(); i++)
    {
      JSONObject object = pJsonArr.getJSONObject(i);
      PropertySetKeyDefDataObject propertSetKetDef = new PropertySetKeyDefDataObject();
      Long propertyDefinitionId = object.getLong("propertyDefinitionId");
      Long propertySetId = object.getLong("propertySetId");

      propertSetKetDef.setPropertyDefinitionId(propertyDefinitionId);
      propertSetKetDef.setPropertySetId(propertySetId);

      results.add(propertSetKetDef);
    }

    LOGGER.exiting(CLZ_NAM, methodName, results.size());
    return results;
  }

  @Override
  public boolean equals(Object o) 
  {
      if (o == this)
          return true;
      if (!(o instanceof PropertySetKeyDefDataObject))
          return false;
      PropertySetKeyDefDataObject other = (PropertySetKeyDefDataObject) o;
      System.out.println(this);
      System.out.println(other);
      return Long.compare(this.mPropertySetId, other.mPropertySetId) == 0 && Long.compare(this.mPropertyDefinitionId, other.mPropertyDefinitionId) == 0;
  }

  @Override
  public final int hashCode() 
  {
      int result = 17;
      result = 31 * result + mPropertySetId.hashCode();
      result = 31 * result + mPropertyDefinitionId.hashCode();
      return result;
  }

  @Override
  public String toString() 
  {
    return "PropertySetKeyDefDataObject={PropertySetId=" + mPropertySetId + ", PropertyDefinitionId=" + mPropertyDefinitionId + "}";
  }
}
