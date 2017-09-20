// Copyright 2004-present Facebook. All Rights Reserved.

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
