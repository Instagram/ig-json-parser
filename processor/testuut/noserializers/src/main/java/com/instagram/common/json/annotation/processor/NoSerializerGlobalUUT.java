/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.noserializers;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/**
 * This class has serializer generation turned off via an argument to javac which is valid for this
 * package.
 */
@JsonType
public class NoSerializerGlobalUUT {

  @JsonField(fieldName = "value")
  String mValue;
}
