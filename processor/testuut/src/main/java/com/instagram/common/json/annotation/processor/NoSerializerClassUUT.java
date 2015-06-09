// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor.uut;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

@JsonType(generateSerializer = JsonType.TriState.NO)
public class NoSerializerClassUUT {

    @JsonField(fieldName = "value")
    String mValue;
}
