package com.instagram.common.json.annotation.processor;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.processor.parent.InterfaceParentUUT;
import com.instagram.common.json.annotation.processor.parent.InterfaceParentWithWrapperUUT;


/**
 * Wrapper for interface tests.
 */
@JsonType
public class WrapperInterfaceUUT {
    @JsonField(fieldName = "interface_parent")
    InterfaceParentUUT mInterfaceParent;

    @JsonField(fieldName = "interface_parent_with_wrapper")
    InterfaceParentWithWrapperUUT mInterfaceParentWithWrapper;
}
