package com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.core.text.getSpans
import androidx.core.text.toSpanned
import io.noties.markwon.Markwon
import io.noties.markwon.core.spans.EmphasisSpan
import io.noties.markwon.core.spans.StrongEmphasisSpan
import org.koin.java.KoinJavaComponent.inject

/**
 *  Uses Markwon to convert a String to a Spannable CharSequence with supported Spans
 */
fun String.parseMarkdown(): CharSequence {
    val markwon by inject<Markwon>(Markwon::class.java)
    return markwon.toMarkdown(this).replaceAndRemoveSpans()
}

/**
 *  Using [getReplacement], replaces compatible Spans and strips incompatible ones.
 */
private fun Spanned.replaceAndRemoveSpans(): Spanned {
    val spans = getSpans<Any>()
    val replacementSpans = spans.mapNotNull { span ->
        val replacement = span.getReplacement() ?: return@mapNotNull null
        val start = getSpanStart(span)
        val end = getSpanEnd(span)
        val flags = getSpanFlags(span)
        ReplacementSpan(replacement, start, end, flags)
    }
    return SpannableStringBuilder(this).apply {
        spans.forEach { removeSpan(it) }
        replacementSpans.forEach {
            setSpan(it.span, it.start, it.end, it.flags)
        }
    }.toSpanned()
}

/**
 *  Gets the Android system equivalent span for this Markwon span, if available. If not, returns
 *  `null` and the span will be stripped.
 */
private fun Any.getReplacement(): Any? {
    return when(this) {
        is EmphasisSpan -> StyleSpan(Typeface.ITALIC)
        is StrongEmphasisSpan -> StyleSpan(Typeface.BOLD)
        is StrikethroughSpan -> this //Already correct
        is UnderlineSpan -> this //Already correct
        is RelativeSizeSpan -> this //Already correct
        is ForegroundColorSpan -> this //Already correct
        else -> null //Remove unsupported span
    }
}

private data class ReplacementSpan(val span: Any, val start: Int, val end: Int, val flags: Int)