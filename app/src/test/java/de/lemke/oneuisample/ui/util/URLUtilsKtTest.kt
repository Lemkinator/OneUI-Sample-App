/*
 * Copyright 2024-2026 Leonard Lemke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lemke.oneuisample.ui.util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class URLUtilsKtTest : ShouldSpec(
    {

        context("withHttps") {
            should("add https:// prefix when no protocol present") {
                "example.com".withHttps() shouldBe "https://example.com"
            }
            should("not modify string already starting with https://") {
                "https://example.com".withHttps() shouldBe "https://example.com"
            }
            should("not modify string already starting with http://") {
                "http://example.com".withHttps() shouldBe "http://example.com"
            }
            should("add prefix to empty string") {
                "".withHttps() shouldBe "https://"
            }
        }

        context("withoutHttps") {
            should("remove https:// prefix") {
                "https://example.com".withoutHttps() shouldBe "example.com"
            }
            should("remove http:// prefix") {
                "http://example.com".withoutHttps() shouldBe "example.com"
            }
            should("remove trailing slash") {
                "https://example.com/".withoutHttps() shouldBe "example.com"
            }
            should("handle string with no protocol and no trailing slash") {
                "example.com".withoutHttps() shouldBe "example.com"
            }
            should("remove both prefix and trailing slash") {
                "http://example.com/".withoutHttps() shouldBe "example.com"
            }
        }

        context("urlEncodeAmpersand") {
            should("replace single & with %26") {
                "a=1&b=2".urlEncodeAmpersand() shouldBe "a=1%26b=2"
            }
            should("replace multiple ampersands") {
                "a&b&c".urlEncodeAmpersand() shouldBe "a%26b%26c"
            }
            should("return string unchanged when no ampersand") {
                "hello world".urlEncodeAmpersand() shouldBe "hello world"
            }
            should("handle empty string") {
                "".urlEncodeAmpersand() shouldBe ""
            }
        }

        context("urlEncode") {
            should("encode space as +") {
                "hello world".urlEncode() shouldBe "hello+world"
            }
            should("encode forward slash") {
                "a/b".urlEncode() shouldBe "a%2Fb"
            }
            should("encode equals sign") {
                "a=b".urlEncode() shouldBe "a%3Db"
            }
            should("leave alphanumeric unchanged") {
                "abc123".urlEncode() shouldBe "abc123"
            }
        }
    },
)
