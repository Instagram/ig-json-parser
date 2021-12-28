/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.dependent;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.processor.MyEnum;
import com.instagram.common.json.annotation.processor.MyEnumHolder;
import java.util.HashMap;
import java.util.List;

/** Wrapper for list of enums */
@JsonType
public class WrapperEnumUUT {
  public static final String ENUM_LIST_KEY = "enum_list";
  public static final String ENUM_MAP_KEY = "enum_map";
  public static final String ENUM_HOLDER_LIST_KEY = "enum_holder_list";
  public static final String ENUM_HOLDER_MAP_KEY = "enum_holder_map";

  @JsonField(fieldName = ENUM_HOLDER_LIST_KEY)
  public List<MyEnumHolder> mMyEnumHolderList;

  @JsonField(fieldName = ENUM_HOLDER_MAP_KEY)
  public HashMap<String, MyEnumHolder> mMyEnumHolderMap;

  @JsonField(fieldName = ENUM_LIST_KEY)
  public List<MyEnum> mMyEnumList;

  @JsonField(fieldName = ENUM_MAP_KEY)
  public HashMap<String, MyEnum> mMyEnumMap;
}
