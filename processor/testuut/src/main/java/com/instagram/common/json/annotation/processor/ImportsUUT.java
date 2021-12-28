/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.uut;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/** Class to exercise imports functionality. */
@JsonType(imports = "java.util.Formatter")
public class ImportsUUT {

  @JsonField(
      fieldName = "string_field",
      valueExtractFormatter =
          "new Formatter().format(\":%%s\", ${parser_object}.getText()).toString()")
  public String mStringField;
}
