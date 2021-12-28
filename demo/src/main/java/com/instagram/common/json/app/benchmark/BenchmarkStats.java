/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.app.benchmark;

import android.os.Debug;
import android.os.SystemClock;

/** Utility class to gather stats. */
class BenchmarkStats {
  enum State {
    INIT,
    BEFORE_CALLED,
    AFTER_CALLED,
    ;
  }

  private Debug.MemoryInfo mMemoryInfoBefore;
  private Debug.MemoryInfo mMemoryInfoAfter;
  private int mAllocCount;
  private int mAllocSize;
  private int mFreeCount;
  private int mFreeSize;
  private long mBeforeTimestamp;
  private long mAfterTimestamp;
  private State mState;

  BenchmarkStats() {
    mMemoryInfoBefore = new Debug.MemoryInfo();
    mMemoryInfoAfter = new Debug.MemoryInfo();
    mState = State.INIT;
  }

  /** Grabs the pre-snapshot. Forces a {@link System#gc()} before collecting stats. */
  synchronized void before() {
    if (mState != State.INIT) {
      throw new IllegalStateException("unexpected state");
    }

    mState = State.BEFORE_CALLED;
    System.gc();
    Debug.getMemoryInfo(mMemoryInfoBefore);
    Debug.startAllocCounting();
    mBeforeTimestamp = SystemClock.elapsedRealtime();
  }

  /** Grabs the post-snapshot. */
  synchronized void after() {
    mAfterTimestamp = SystemClock.elapsedRealtime();
    Debug.stopAllocCounting();
    Debug.getMemoryInfo(mMemoryInfoAfter);

    mAllocCount = Debug.getGlobalAllocCount();
    mAllocSize = Debug.getGlobalAllocSize();
    mFreeCount = Debug.getGlobalFreedCount();
    mFreeSize = Debug.getGlobalFreedSize();

    if (mState != State.BEFORE_CALLED) {
      throw new IllegalStateException("unexpected state");
    }
    mState = State.AFTER_CALLED;
  }

  String renderResultsToText() {
    if (mState != State.AFTER_CALLED) {
      throw new IllegalStateException("attempted to render results before after was called");
    }

    StringBuilder sb = new StringBuilder();

    sb.append("elapsed time: ").append(mAfterTimestamp - mBeforeTimestamp).append("ms\n");
    sb.append("pss delta: ")
        .append(mMemoryInfoAfter.dalvikPss - mMemoryInfoBefore.dalvikPss)
        .append("\n");

    sb.append("alloc count: ").append(mAllocCount).append("\n");
    sb.append("alloc size: ").append(mAllocSize).append("\n");
    sb.append("free count: ").append(mFreeCount).append("\n");
    sb.append("free size: ").append(mFreeSize).append("\n");

    return sb.toString();
  }
}
