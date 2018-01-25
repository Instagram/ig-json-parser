// Copyright 2004-present Facebook. All Rights Reserved.

package com.instagram.common.json.annotation.processor.dependent;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;
import com.instagram.common.json.annotation.processor.parent.InterfaceParentUUT;

@JsonType()
public class InterfaceImplementationUUT implements InterfaceParentUUT {
    public static final String TYPE_NAME = "InterfaceImplementationUUT";

    @JsonField(fieldName = "stringField")
    public String stringField;

    @Override
    public String getTypeNameYeah() {
        return TYPE_NAME;
    }
}
