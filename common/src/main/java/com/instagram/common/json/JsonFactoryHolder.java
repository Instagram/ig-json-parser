/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json;

import com.fasterxml.jackson.core.JsonFactory;

/** Holds the {@link JsonFactory} singleton. */
public class JsonFactoryHolder {
  public static final JsonFactory APP_FACTORY = new JsonFactory();
}
