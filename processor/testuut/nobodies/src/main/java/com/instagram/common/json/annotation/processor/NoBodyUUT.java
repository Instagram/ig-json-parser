package com.instagram.common.json.annotation.processor.nobodies;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/**
 * This class has method body generation turned off.
 */
@JsonType
public class NoBodyUUT {

    @JsonField(fieldName = "value")
    public String mValue;
}
