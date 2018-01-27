package com.instagram.common.json.annotation.processor.parent;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/**
 * A default interface implementation.
 */
@JsonType
public class InterfaceImplementationUUT implements InterfaceParentUUT {
    @JsonField(fieldName = "stringField")
    public String mStringField;
}
