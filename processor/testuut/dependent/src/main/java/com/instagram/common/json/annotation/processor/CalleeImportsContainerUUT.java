package com.instagram.common.json.annotation.processor.dependent;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/**
 * A container to exercise the callee imports tools
 */
@JsonType
public class CalleeImportsContainerUUT {
  @JsonField(fieldName = "callee_ref")
  public com.instagram.common.json.annotation.processor.parent.CalleeImportsUUT mCalleeImports;
}
