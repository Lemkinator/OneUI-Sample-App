package de.lemke.oneuisample.domain

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_MARK_MARK
import android.text.style.TextAppearanceSpan
import dagger.hilt.android.qualifiers.ActivityContext
import dev.oneuiproject.oneui.design.R
import javax.inject.Inject

class SearchHighlighter @Inject constructor(
    @ActivityContext private val context: Context
) {
    operator fun invoke(text: String, textToBold: String?): SpannableStringBuilder = invoke(text, textToBold, -1)

    operator fun invoke(text: String, textToBold: String?, lengthBefore: Int): SpannableStringBuilder {
        if (textToBold.isNullOrBlank()) return SpannableStringBuilder(text)
        return makeSectionOfTextBold(SpannableStringBuilder(text), HashSet(textToBold.split(" ")), lengthBefore)
    }

    private fun makeSectionOfTextBold(builder: SpannableStringBuilder, textToBold: String): SpannableStringBuilder {
        var text = builder.toString()
        if (textToBold.isEmpty() || !text.contains(textToBold, ignoreCase = true)) return builder
        var startingIndex = text.indexOf(textToBold, ignoreCase = true)
        var endingIndex = startingIndex + textToBold.length
        var offset = 0 //for multiple replaces
        var firstSearchIndex = text.length
        while (startingIndex >= 0) {
            builder.setSpan(
                TextAppearanceSpan(context, R.style.OneUI_SearchHighlightedTextAppearance),
                offset + startingIndex,
                offset + endingIndex,
                SPAN_MARK_MARK
            )
            if (startingIndex < firstSearchIndex) firstSearchIndex = startingIndex
            text = text.substring(endingIndex)
            offset += endingIndex
            startingIndex = text.indexOf(textToBold, ignoreCase = true)
            endingIndex = startingIndex + textToBold.length
        }
        return builder
    }

    private fun makeSectionOfTextBold(
        spannableStringBuilder: SpannableStringBuilder,
        textsToBold: HashSet<String>,
        lengthBefore: Int
    ): SpannableStringBuilder {
        var builder = spannableStringBuilder
        val text = builder.toString()
        var firstSearchIndex = text.length
        for (textItem in textsToBold) {
            if (text.contains(textItem, ignoreCase = true)) {
                firstSearchIndex = text.indexOf(textItem, ignoreCase = true)
                builder = makeSectionOfTextBold(builder, textItem)
            }
        }
        val start = 0.coerceAtLeast(firstSearchIndex - lengthBefore)
        return if (firstSearchIndex != text.length && lengthBefore >= 0 && start > 0) builder.replace(0, start, "...")
        else builder
    }
}