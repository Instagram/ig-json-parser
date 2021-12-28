/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.parent;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/** A class to test whether the {@link JsonType#typeFormatterImports} parameter works. */
@JsonType(
    valueExtractFormatter =
        "TypeFormatterImportsCompanionUUT__JsonHelper.parseFromJson(${parser_object})",
    typeFormatterImports = {
      "com.instagram.common.json.annotation.processor.parent.TypeFormatterImportsCompanionUUT__JsonHelper"
    })
public class TypeFormatterImportsUUT {
  @JsonField(fieldName = "string_field")
  public String mString;
}
