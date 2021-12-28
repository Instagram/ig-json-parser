/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import java.util.List;
import javax.annotation.Nullable;

@JsonType(strict = true)
public class ImmutableObject {

  static final Integer DATA_4_DEFAULT = 1234;

  private final String mData1;
  private final Integer mData2;

  @JsonField(fieldName = "data3")
  @Nullable
  List<Integer> mData3;

  @JsonField(fieldName = "data4")
  Integer mData4 = DATA_4_DEFAULT;

  public ImmutableObject(
      @Nullable @JsonField(fieldName = "data1") String data1,
      @JsonField(fieldName = "data2") Integer data2) {
    mData1 = data1;
    mData2 = data2;
  }

  public String getData1() {
    return mData1;
  }

  public Integer getData2() {
    return mData2;
  }
}
