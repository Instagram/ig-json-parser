/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.uut;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType(generateSerializer = JsonType.TriState.NO)
public class NoSerializerClassUUT {

  enum DummyEnum {
    A,
    B;
  }

  @JsonField(fieldName = "value")
  String mValue;

  /**
   * Ensure that we don't have to provide a serializeCodeFormatter for enums if serialization is
   * disabled.
   */
  @JsonField(
      fieldName = "dummyEnum",
      valueExtractFormatter = "NoSerializerClassUUT.DummyEnum.valueOf(${parser_object}.getText())")
  DummyEnum dummyEnum;
}
