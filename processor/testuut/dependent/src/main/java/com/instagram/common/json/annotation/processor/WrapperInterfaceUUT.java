package com.instagram.common.json.annotation.processor;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.processor.parent.InterfaceParentDynamicUUT;
import com.instagram.common.json.annotation.processor.parent.InterfaceParentNoFormattersUUT;
import com.instagram.common.json.annotation.processor.parent.InterfaceParentUUT;
import com.instagram.common.json.annotation.processor.parent.InterfaceParentWithWrapperUUT;

import java.util.List;


/**
 * Wrapper for interface tests.
 */
@JsonType
public class WrapperInterfaceUUT {
    @JsonField(fieldName = "interface_parent")
    InterfaceParentUUT mInterfaceParent;

    @JsonField(fieldName = "interface_parent_with_wrapper")
    InterfaceParentWithWrapperUUT mInterfaceParentWithWrapper;

    @JsonField(
            fieldName = "interface_parent_no_formatters",
            valueExtractFormatter =
                    "com.instagram.common.json.annotation.processor.parent.InterfaceImplementationUUT__JsonHelper"
                            + ".parseFromJson(${parser_object})",
            serializeCodeFormatter =
                    "com.instagram.common.json.annotation.processor.parent.InterfaceImplementationUUT__JsonHelper"
                            + ".serializeToJson(${generator_object}, "
                            + "(com.instagram.common.json.annotation.processor.parent.InterfaceImplementationUUT)"
                            + "${object_varname}.${field_varname}, "
                            + "true)")
    InterfaceParentNoFormattersUUT mInterfaceParentNoFormatters;

    @JsonField(fieldName = "interface_parent_dynamic")
    InterfaceParentDynamicUUT mInterfaceParentDynamic;

    @JsonField(fieldName = "interface_parent_list")
    List<InterfaceParentUUT> mInterfaceParentList;
}
