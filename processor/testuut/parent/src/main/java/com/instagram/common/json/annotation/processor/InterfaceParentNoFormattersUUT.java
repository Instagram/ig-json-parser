/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.instagram.common.json.annotation.processor.parent;

import com.instagram.common.json.annotation.JsonType;

/**
 * An interface to verify that serialization/parsing can work without formatters on the interface
 * itself.
 */
@JsonType
public interface InterfaceParentNoFormattersUUT {}
