/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.app.igmodel;

import java.io.IOException;

/** Helper class to parse the model. */
public class IgModelWorker {
  public IgModelRequest parseFromString(String input) throws IOException {
    return IgModelRequest__JsonHelper.parseFromJson(input);
  }

  public IgListOfModels parseListFromString(String input) throws IOException {
    return IgListOfModels__JsonHelper.parseFromJson(input);
  }
}
