package id.islaami.playmi.util

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.*
import android.text.Annotation
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Patterns
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import id.islaami.playmi.R
import java.text.NumberFormat
import java.util.*

fun String?.isValidEmail(): Boolean =
    this != null && isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String?.isValidPassword(): Boolean =
    this != null && isNotEmpty() && this.length >= 6

fun String?.toDouble(): Double {
    if (this != null && this.isNotEmpty()) return this.toDouble()
    return 0.0
}

fun String?.digitGrouping(): String {
    if (this != null && this.isNotEmpty())
        return NumberFormat.getInstance(Locale.ITALY).format(this.toDouble().toInt())
    return "0"
}

fun String?.fromHtmlToString() = this?.let {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY).toString()
    else HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
} ?: ""

fun String?.fromHtmlToSpanned() : Spanned? = this?.let {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY)
    else HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun String?.containsAlphabetAndNumber(): Boolean =
    this?.matches(Regex("^(?=.*[0-9])(?=.*[a-zA-Z])([a-zA-Z0-9]+)\$")) == true

fun createClickableString(
    context: Context,
    stringRes: Int,
    key: String,
    isUnderLine: Boolean = false,
    foregroundColor: Int = R.color.accent,
    backgroundColor: Int = R.color.white,
    onClickAction: () -> Unit
): SpannableString {
    val fulltext = context.getText(stringRes) as SpannedString
    val spannableString = SpannableString(fulltext)
    val annotations = fulltext.getSpans(0, fulltext.length, Annotation::class.java)
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onClickAction()
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = isUnderLine
        }
    }

    annotations.find { it.value == key }.let {
        spannableString.apply {
            setSpan(
                clickableSpan,
                fulltext.getSpanStart(it),
                fulltext.getSpanEnd(it),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                StyleSpan(Typeface.NORMAL),
                fulltext.getSpanStart(it),
                fulltext.getSpanEnd(it),
                0
            )
            setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        foregroundColor
                    )
                ),
                fulltext.getSpanStart(it),
                fulltext.getSpanEnd(it),
                0
            )
            setSpan(
                BackgroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        backgroundColor
                    )
                ),
                fulltext.getSpanStart(it),
                fulltext.getSpanEnd(it),
                0
            )
        }
    }

    return spannableString
}

fun createClickableString(
    context: Context,
    stringRes: Int,
    key: String,
    isUnderLine: Boolean = false,
    foregroundColor: Int = R.color.accent,
    onClickAction: () -> Unit
): SpannableString {
    val fulltext = context.getText(stringRes) as SpannedString
    val spannableString = SpannableString(fulltext)
    val annotations = fulltext.getSpans(0, fulltext.length, Annotation::class.java)
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onClickAction()
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = isUnderLine
        }
    }

    annotations.find { it.value == key }.let {
        spannableString.apply {
            setSpan(
                clickableSpan,
                fulltext.getSpanStart(it),
                fulltext.getSpanEnd(it),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                StyleSpan(Typeface.NORMAL),
                fulltext.getSpanStart(it),
                fulltext.getSpanEnd(it),
                0
            )
            setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        foregroundColor
                    )
                ),
                fulltext.getSpanStart(it),
                fulltext.getSpanEnd(it),
                0
            )
        }
    }

    return spannableString
}
