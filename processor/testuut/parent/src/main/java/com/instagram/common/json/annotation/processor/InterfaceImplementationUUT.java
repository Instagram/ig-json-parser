/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.parent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import java.io.IOException;

/** A default interface implementation. */
@JsonType
public class InterfaceImplementationUUT
    implements InterfaceParentUUT,
        InterfaceParentWithWrapperUUT,
        InterfaceParentNoFormattersUUT,
        InterfaceParentDynamicUUT {
  public static final String TYPE_NAME = "InterfaceImplementationUUT";
  public static final DynamicDispatchAdapter.TypeAdapter<InterfaceParentDynamicUUT> ADAPTER =
      new DynamicDispatchAdapter.TypeAdapter<InterfaceParentDynamicUUT>() {
        @Override
        public void serializeToJson(JsonGenerator generator, InterfaceParentDynamicUUT object)
            throws IOException {
          InterfaceImplementationUUT__JsonHelper.serializeToJson(
              generator, (InterfaceImplementationUUT) object, true);
        }

        @Override
        public InterfaceParentDynamicUUT parseFromJson(JsonParser parser) throws IOException {
          return InterfaceImplementationUUT__JsonHelper.parseFromJson(parser);
        }
      };

  @JsonField(fieldName = "stringField")
  public String mStringField;

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }
}
