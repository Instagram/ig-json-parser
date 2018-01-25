package com.instagram.common.json.annotation.processor.parent;

import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.JsonTypeName;

/**
 * Interface parent to test that polymorphic deserialization works.
 */
@JsonType
public interface InterfaceParentUUT {
    @JsonTypeName
    String getTypeNameYeah();
}
