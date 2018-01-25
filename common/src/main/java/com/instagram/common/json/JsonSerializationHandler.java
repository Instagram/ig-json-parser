package com.instagram.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

public interface JsonSerializationHandler<T> {
    void serializeToJson(JsonGenerator generator, T object) throws IOException;
    T parseFromJson(JsonParser parser) throws IOException;
}
