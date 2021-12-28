/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.parent;

import com.instagram.common.json.annotation.JsonType;

/** An interface parent that performs static-ish dispatch with a wrapper class. */
@JsonType(
    valueExtractFormatter =
        "com.instagram.common.json.annotation.processor.parent.InterfaceParentWrapperUUT__JsonHelper"
            + ".parseFromJson(${parser_object}).getInterfaceParentWithWrapperUUT()",
    serializeCodeFormatter =
        "com.instagram.common.json.annotation.processor.parent.InterfaceParentWrapperUUT__JsonHelper"
            + ".serializeToJson(${generator_object}, "
            + "com.instagram.common.json.annotation.processor.parent.InterfaceParentWrapperUUT.from("
            + ""
            + "${subobject}), "
            + "true)")
public interface InterfaceParentWithWrapperUUT {}
