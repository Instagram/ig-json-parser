/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.parent;

import com.instagram.common.json.annotation.JsonType;

@JsonType(
    serializeCodeFormatter =
        "com.instagram.common.json.annotation.processor.parent.InterfaceParentDynamicUUTHelper.DISPATCHER.serializeToJson("
            + "${generator_object}, ${subobject})",
    valueExtractFormatter =
        "com.instagram.common.json.annotation.processor.parent.InterfaceParentDynamicUUTHelper.DISPATCHER.parseFromJson("
            + "${parser_object})")
public interface InterfaceParentDynamicUUT extends DynamicDispatchAdapter.TypeNameProvider {}
