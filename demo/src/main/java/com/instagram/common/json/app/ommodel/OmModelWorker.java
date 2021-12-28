/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.app.ommodel;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/** Helper class to parse the model. */
public class OmModelWorker {
  private static final ObjectMapper sObjectMapper = new ObjectMapper();

  public OmModelRequest parseFromString(String input) throws IOException {
    return sObjectMapper.readValue(input, OmModelRequest.class);
  }

  public OmListOfModels parseListFromString(String input) throws IOException {
    return sObjectMapper.readValue(input, OmListOfModels.class);
  }
}
