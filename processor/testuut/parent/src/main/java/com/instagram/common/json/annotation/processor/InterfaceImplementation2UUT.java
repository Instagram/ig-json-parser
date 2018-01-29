package com.instagram.common.json.annotation.processor.parent;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/**
 * A second implementation of {@link com.instagram.common.json.annotation.processor.parent.InterfaceParentWithWrapperUUT}.
 */
@JsonType
public class InterfaceImplementation2UUT implements InterfaceParentWithWrapperUUT {
    @JsonField(fieldName = "integer_field")
    public int mIntegerField;
}
