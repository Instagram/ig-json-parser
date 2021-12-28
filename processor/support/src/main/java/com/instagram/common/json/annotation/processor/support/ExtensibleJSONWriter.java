/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.support;

import java.io.Writer;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * This is a subclass of {@link JSONWriter} that allows a function to be called within a sequence of
 * commands. This is syntactic sugar for constructs like:
 *
 * <p>
 *
 * <pre>
 *   writer.array()
 *           .value(XXX)
 *           .value(YYY)
 *           .object()
 *           // call some function that writes the object data
 *           .extend(EXTENDER)
 *           .endObject()
 *         .endArray();
 * </pre>
 *
 * <p>Without this syntactic sugar, one would have to have one block for everything leading up to
 * the function call, the function call, and the remaining items. This also separates the opening
 * <code>array()</code>/<code>object()</code> calls from their closing counterparts.
 *
 * <p>
 */
public class ExtensibleJSONWriter extends JSONWriter {

  /**
   * Implementations of this interface can be passed into {@link #extend(Extender)} to do the actual
   * work.
   */
  public interface Extender {
    void extend(ExtensibleJSONWriter writer) throws JSONException;
  }

  public ExtensibleJSONWriter(Writer writer) {
    super(writer);
  }

  /** Execute the {@link Extender#extend(Extender)} method. */
  public ExtensibleJSONWriter extend(Extender extender) throws JSONException {
    extender.extend(this);
    return this;
  }

  /////////////
  // The remaining methods are simply overriden methods of {@link JSONWriter} that return an
  // {@link ExtensibleJSONWriter}.
  @Override
  public ExtensibleJSONWriter array() throws JSONException {
    super.array();
    return this;
  }

  @Override
  public ExtensibleJSONWriter endArray() throws JSONException {
    super.endArray();
    return this;
  }

  @Override
  public ExtensibleJSONWriter endObject() throws JSONException {
    super.endObject();
    return this;
  }

  @Override
  public ExtensibleJSONWriter key(String s) throws JSONException {
    super.key(s);
    return this;
  }

  @Override
  public ExtensibleJSONWriter object() throws JSONException {
    super.object();
    return this;
  }

  @Override
  public ExtensibleJSONWriter value(boolean b) throws JSONException {
    super.value(b);
    return this;
  }

  @Override
  public ExtensibleJSONWriter value(double v) throws JSONException {
    super.value(v);
    return this;
  }

  @Override
  public ExtensibleJSONWriter value(long l) throws JSONException {
    super.value(l);
    return this;
  }

  @Override
  public ExtensibleJSONWriter value(Object o) throws JSONException {
    super.value(o);
    return this;
  }
}
