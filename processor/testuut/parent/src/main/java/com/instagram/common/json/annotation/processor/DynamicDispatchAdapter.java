/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.parent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import java.util.HashMap;
import javax.annotation.Nullable;

public class DynamicDispatchAdapter<T extends DynamicDispatchAdapter.TypeNameProvider> {
  public interface TypeNameProvider {
    public String getTypeName();
  }

  public interface TypeAdapter<T> {
    void serializeToJson(JsonGenerator generator, T object) throws IOException;

    T parseFromJson(JsonParser parser) throws IOException;
  }

  private HashMap<String, TypeAdapter<T>> mAdapterMap = new HashMap<>();

  public void register(String typeName, TypeAdapter<T> adapter) {
    if (mAdapterMap.containsKey(typeName)) {
      String message =
          String.format(
              "Duplicate handler name. %s is already mapped to an instance of %s",
              typeName, mAdapterMap.get(typeName));
      throw new IllegalArgumentException(message);
    }
    mAdapterMap.put(typeName, adapter);
  }

  public void unregister(String typeName) {
    mAdapterMap.remove(typeName);
  }

  public T parseFromJson(JsonParser parser) throws IOException {
    if (parser.getCurrentToken() != JsonToken.START_ARRAY) {
      parser.skipChildren();
      return null;
    }

    parser.nextToken();
    if (parser.getCurrentToken() != JsonToken.VALUE_STRING) {
      parser.skipChildren();
      return null;
    }

    String typeName = parser.getText();
    parser.nextToken();
    final T instance = getAdapter(typeName).parseFromJson(parser);
    parser.nextToken();
    return instance;
  }

  private TypeAdapter<T> getAdapter(String typeName) {
    final @Nullable TypeAdapter<T> adapter = mAdapterMap.get(typeName);
    if (adapter == null) {
      final String message = String.format("No TypeAdapter registered for type name: %s", typeName);
      throw new IllegalArgumentException(message);
    }

    return adapter;
  }

  public void serializeToJson(JsonGenerator generator, T object) throws IOException {
    generator.writeStartArray();
    generator.writeString(object.getTypeName());
    getAdapter(object.getTypeName()).serializeToJson(generator, object);
    generator.writeEndArray();
  }
}
