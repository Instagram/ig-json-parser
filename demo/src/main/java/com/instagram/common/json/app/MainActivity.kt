/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.instagram.common.json.app.benchmark.BenchmarkActivity
import com.instagram.common.json.app.playground.PlaygroundActivity
import com.instagram.jsonbenchmark.app.R
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)

    playground.setOnClickListener { startActivity(Intent(this, PlaygroundActivity::class.java)) }

    benchmark.setOnClickListener { startActivity(Intent(this, BenchmarkActivity::class.java)) }
  }
}
