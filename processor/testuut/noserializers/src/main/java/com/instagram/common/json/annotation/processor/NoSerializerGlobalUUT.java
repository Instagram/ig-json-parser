package com.instagram.common.json.annotation.processor.noserializers;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/**
 * This class has serializer generation turned off via an argument to javac which is valid for this package.
 */
@JsonType
public class NoSerializerGlobalUUT {

    @JsonField(fieldName = "value")
    String mValue;
}
