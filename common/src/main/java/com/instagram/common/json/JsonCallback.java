/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json;

import java.io.IOException;

/**
 * An interface for receiving callbacks when deserializing strict JsonType classes. This can only be
 * provided by wrapping the JsonParser that is supplied to the parseFromJson method, so should be
 * considered an advanced feature for the library.
 */
public interface JsonCallback {

  /**
   * The interface for providing the callback. The JsonParser must implement this in order for it to
   * be accessed. The default JsonFactoryHolder does not supply this so you must set it up and
   * provide your own. For an example of how this could work, see the code in JavaStrictSupportTest.
   */
  public interface Provider {
    public JsonCallback getJsonCallback();
  }

  /**
   * An exception that can be thrown when onUnexpectedNull is called. The callback as the option to
   * noop, and allow the deserialization to continue. However it may be useful to throw an exception
   * and reject the deserialized objects. For instance it may allow the calling code to treat the
   * exception as an issue similar to a network error when a network response is being deserialized.
   */
  public static class JsonDeserializationException extends IOException {
    public JsonDeserializationException() {
      super();
    }

    public JsonDeserializationException(String message) {
      super(message);
    }

    public JsonDeserializationException(String message, Throwable cause) {
      super(message, cause);
    }

    public JsonDeserializationException(Throwable cause) {
      super(cause);
    }
  }

  /**
   * This will be called when the json payload does not match the type annotations of the class. It
   * optionally throws JsonDeserializationException depending on whether the implementer wants to
   * allow deserialization to continue or stop immediately.
   */
  public void onUnexpectedNull(String fieldName, String className)
      throws JsonDeserializationException;
}
