/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.uut;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import java.io.IOException;

@JsonType
public class CustomParseContainerUUT {

  public static final String INNER_FIELD_NAME = "inner";

  @JsonField(fieldName = "inner")
  CustomParseUUT mCustomParseUUT;

  public CustomParseUUT getCustomParseUUT() {
    return mCustomParseUUT;
  }

  @JsonType(valueExtractFormatter = "${subobject_class}.parseFromJson(${parser_object})")
  public static class CustomParseUUT {
    public static final String STRING_FIELD_NAME = "string_field";

    private String stringField;

    public String getStringField() {
      return stringField;
    }

    public static CustomParseUUT parseFromJson(JsonParser jp) throws IOException {
      CustomParseUUT instance = new CustomParseUUT();

      // validate that we're on the right token
      if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
        jp.skipChildren();
        return null;
      }

      while (jp.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = jp.getCurrentName();
        jp.nextToken();

        if (STRING_FIELD_NAME.equals(fieldName)) {
          instance.stringField = jp.getText();
        }

        jp.skipChildren();
      }

      return instance;
    }
  }
}
