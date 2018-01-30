package com.instagram.common.json.annotation.processor.uut;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

/**
 * Class to exercise imports functionality.
 */
@JsonType(imports = "java.util.Formatter")
public class ImportsUUT {

    @JsonField(fieldName = "string_field",
            valueExtractFormatter =
                "new Formatter().format(\":%%s\", ${parser_object}.getText()).toString()")
    public String mStringField;
}
