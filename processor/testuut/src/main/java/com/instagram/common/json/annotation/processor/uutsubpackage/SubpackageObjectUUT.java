package uutsubpackage;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/**
 * UUT for embedding a subobject which lives in a different subpackage.
 * Subobject of the class should be imported
 */
public class SubpackageObjectUUT {

  @JsonType
  public class SubpackageSubobjectUUT {
    public static final String INT_FIELD_NAME = "int";

    @JsonField(fieldName = INT_FIELD_NAME)
    public int intField;
  }
}
