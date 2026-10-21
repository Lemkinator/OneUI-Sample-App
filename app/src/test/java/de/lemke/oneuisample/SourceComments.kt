/*
 * Copyright 2022-2026 Leonard Lemke
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
package de.lemke.oneuisample

import java.io.File

internal object SourceComments {
    private const val RAW_QUOTE = "\"\"\""
    private const val TEMPLATE_START = "\${"
    private const val BACKTICK_BLANKED = "`'\""
    private const val LINE_BREAKS = "\r\n"

    /** [source] with every comment blanked to spaces; strings, chars and backtick identifiers stay. Line breaks stay. */
    fun withoutComments(source: String): String {
        val code = StringBuilder(source)
        var index = 0
        while (index < source.length) {
            val end = source.literalEnd(index)
            if (source.startsWith("//", index) || source.startsWith("/*", index)) {
                for (blanked in index until end) if (code[blanked] !in LINE_BREAKS) code[blanked] = ' '
            }
            index = maxOf(end, index + 1)
        }
        return code.toString()
    }

    /** The end of the comment, literal or backtick identifier that starts at [start], or [start] if none starts there. */
    private fun String.literalEnd(start: Int): Int =
        when {
            startsWith("//", start) -> indexOf('\n', start).takeIf { it >= 0 } ?: length
            startsWith("/*", start) -> blockCommentEnd(start)
            startsWith(RAW_QUOTE, start) -> stringEnd(start + RAW_QUOTE.length, raw = true)
            this[start] == '"' -> stringEnd(start + 1, raw = false)
            this[start] == '\'' -> charEnd(start + 1)
            this[start] == '`' -> indexOf('`', start + 1).takeIf { it >= 0 }?.plus(1) ?: length
            else -> start
        }

    private fun String.blockCommentEnd(start: Int): Int {
        var depth = 0
        var index = start
        while (index < length) {
            val delimiter =
                when {
                    startsWith("/*", index) -> 1
                    startsWith("*/", index) -> -1
                    else -> 0
                }
            depth += delimiter
            index += if (delimiter == 0) 1 else 2
            if (depth == 0) return index
        }
        return length
    }

    private fun String.stringEnd(
        start: Int,
        raw: Boolean,
    ): Int {
        var index = start
        while (index < length && !isStringClose(index, raw)) {
            index =
                when {
                    !raw && this[index] == '\\' -> index + 2
                    startsWith(TEMPLATE_START, index) -> templateEnd(index + TEMPLATE_START.length)
                    else -> index + 1
                }
        }
        return when {
            index >= length -> length
            raw -> skipQuotes(index)
            else -> index + 1
        }
    }

    private fun String.isStringClose(
        index: Int,
        raw: Boolean,
    ): Boolean = if (raw) startsWith(RAW_QUOTE, index) else this[index] == '"'

    /** A raw string may end in more than three quotes; the extra ones belong to its content. */
    private fun String.skipQuotes(start: Int): Int {
        var index = start
        while (index < length && this[index] == '"') index++
        return index
    }

    private fun String.templateEnd(start: Int): Int {
        var depth = 1
        var index = start
        while (index < length && depth > 0) {
            val literalEnd = literalEnd(index)
            when {
                literalEnd > index -> index = literalEnd - 1
                this[index] == '{' -> depth++
                this[index] == '}' -> depth--
            }
            index++
        }
        return index
    }

    private fun String.charEnd(start: Int): Int {
        val contentEnd = if (getOrNull(start) == '\\') start + 2 else start + 1
        return indexOf('\'', contentEnd).takeIf { it >= 0 }?.plus(1) ?: length
    }
}

/** [projectPath] with `/` as the path separator; Konsist reports it with the separator of the platform. */
internal fun portableProjectPath(
    projectPath: String,
    separator: Char = File.separatorChar,
): String = projectPath.replace(separator, '/')
