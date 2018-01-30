package com.instagram.common.json.annotation.processor.parent;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/**
 * A class to test whether the {@link JsonType#calleeImports} parameter works.
 */
@JsonType(
    valueExtractFormatter = "CalleeImportsCompanionUUT__JsonHelper.parseFromJson(${parser_object})",
    calleeImports = {
        "com.instagram.common.json.annotation.processor.parent.CalleeImportsCompanionUUT__JsonHelper"
    })
public class CalleeImportsUUT {
  @JsonField(fieldName = "string_field")
  public String mString;

}