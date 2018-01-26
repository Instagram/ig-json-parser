package com.instagram.common.json.annotation.processor;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.processor.parent.InterfaceParentUUT;

/**
 * Wraps {@link com.instagram.common.json.annotation.processor.parent.InterfaceParentUUT}.
 */
@JsonType
public class WrapperInterfaceUUT {
    @JsonField(fieldName = "interface_parent")
    InterfaceParentUUT mInterfaceParent;
}
