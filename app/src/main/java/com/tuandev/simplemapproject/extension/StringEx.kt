package com.tuandev.simplemapproject.extension

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan


fun Any.toFloatOrNull(): Float? {
    return try {
        this.toString().toFloat()
    } catch (e: Exception) {
        null
    }
}

fun Any.toDoubleOrNull(): Double? {
    return try {
        this.toString().toDouble()
    } catch (e: Exception) {
        null
    }
}

fun Any.toIntOrNull(): Int? {
    return try {
        this.toString().toInt()
    } catch (e: Exception) {
        null
    }
}

fun Float.toRoundedFloat(roundNumber: Int): Float {
    return try {
        String.format("%.${roundNumber}f", this).toFloat()
    } catch (e: Exception) {
        this
    }
}

fun String.handleHighlightSpannable(highlightKeywords: List<String>): SpannableStringBuilder {
    val span = SpannableStringBuilder(this)
    for (keyword in highlightKeywords) {
        var offset = 0
        var start: Int
        val len = keyword.length
        start = indexOf(keyword, offset, true)
        while (start >= 0) {
            val spanStyle = StyleSpan(Typeface.BOLD)
            span.setSpan(spanStyle, start, start + len, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            offset = start + len
            start = indexOf(keyword, offset, true)
        }
    }
    return span
}


