/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.checkflaggedapis

import android.aconfig.Aconfig
import com.android.tradefed.testtype.DeviceJUnit4ClassRunner
import com.android.tradefed.testtype.junit4.BaseHostJUnit4Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

private val API_SIGNATURE =
    """
      // Signature format: 2.0
      package android {
        public final class Clazz {
          ctor public Clazz();
          field @FlaggedApi("android.flag.foo") public static final int FOO = 1; // 0x1
        }
      }
"""
        .trim()

private val PARSED_FLAGS =
    {
      val parsed_flag =
          Aconfig.parsed_flag
              .newBuilder()
              .setPackage("android.flag")
              .setName("foo")
              .setState(Aconfig.flag_state.ENABLED)
              .setPermission(Aconfig.flag_permission.READ_ONLY)
              .build()
      val parsed_flags = Aconfig.parsed_flags.newBuilder().addParsedFlag(parsed_flag).build()
      val binaryProto = ByteArrayOutputStream()
      parsed_flags.writeTo(binaryProto)
      ByteArrayInputStream(binaryProto.toByteArray())
    }()

@RunWith(DeviceJUnit4ClassRunner::class)
class CheckFlaggedApisTest : BaseHostJUnit4Test() {
  @Test
  fun testParseApiSignature() {
    val expected = setOf(Pair(Symbol("android.Clazz.FOO"), Flag("android.flag.foo")))
    val actual = parseApiSignature("in-memory", API_SIGNATURE.byteInputStream())
    assertEquals(expected, actual)
  }

  @Test
  fun testParseFlagValues() {
    val expected: Map<Flag, Boolean> = mapOf(Flag("android.flag.foo") to true)
    val actual = parseFlagValues(PARSED_FLAGS)
    assertEquals(expected, actual)
  }
}
