package com.instagram.common.json.annotation.processor.parent;

import com.instagram.common.json.annotation.JsonType;

/**
 * Interface parent to test that polymorphic deserialization works.
 */
@JsonType(
        valueExtractFormatter =
        "com.instagram.common.json.annotation.processor.parent.InterfaceImplementationUUT__JsonHelper"
                + ".parseFromJson(${parser_object})",
        serializeCodeFormatter =
                "com.instagram.common.json.annotation.processor.parent.InterfaceImplementationUUT__JsonHelper"
                + ".serializeToJson(${generator_object}, "
                + "(com.instagram.common.json.annotation.processor.parent.InterfaceImplementationUUT)"
                + "${subobject}, "
                + "true)")
public interface InterfaceParentUUT {
}
