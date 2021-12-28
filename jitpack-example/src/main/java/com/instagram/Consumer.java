/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram;

import java.io.IOException;

public class Consumer {
  public static void main(String[] args) {
    try {
      Dessert parsed = Dessert__JsonHelper.parseFromJson("{\"type\": \"macaron\"}");
      System.out.println("dessert type: " + parsed.type);
    } catch (IOException ex) {
      System.out.println("fatal: " + ex);
    }
  }
}
