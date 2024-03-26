import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.annotation.ColorInt
import java.util.regex.Pattern

data class StyledTextOption(
    @ColorInt val textColor: Int,
    val style: Int = 0,
    val onClick: (() -> Unit)? = null
) {
    companion object {
        const val STYLE_NONE = 0
        const val STYLE_BOLD = 1
        const val STYLE_UNDERLINE = 2
    }
}

private enum class Style(val attrValue: Int) {
    BOLD(STYLE_BOLD),
    UNDERLINE(STYLE_UNDERLINE)
}

private data class MatchData(
    val range: IntRange,
    val text: String,
    val option: StyledTextOption
)

fun String.setStyledText(
    vararg options: StyledTextOption
): SpannableStringBuilder {
    val spannableText = SpannableStringBuilder(PatternUtils.replaceHtml(this))
    val matchDataList = mutableListOf<MatchData>()

    var regexIndex = 1
    options.iterator().forEach { option ->
        val urlTagPattern = Pattern.compile("<s$regexIndex>(.*?)</s$regexIndex>")
        val urlTagMatcher = urlTagPattern.matcher(spannableText)

        while (urlTagMatcher.find()) {
            val startIndex = urlTagMatcher.start()
            val endIndex = urlTagMatcher.end()
            val textWithoutTags = urlTagMatcher.group(1) ?: break
            matchDataList.add(
                MatchData(
                    startIndex..endIndex,
                    textWithoutTags,
                    option
                )
            )
        }
        regexIndex++
    }

    var shift = 0
    matchDataList.sortBy { it.range.last }
    matchDataList.forEach { data ->
        val firstIndex = data.range.first - shift
        val lastIndex = data.range.last - shift
        spannableText.replace(firstIndex, lastIndex, data.text)

        val urlTagSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color = data.option.textColor

                var styleOptionInt = data.option.style
                Style.values().reversed().forEach {
                    if (styleOptionInt >= it.attrValue) {
                        styleOptionInt -= it.attrValue
                        when (it) {
                            BOLD -> textPaint.typeface = Typeface.DEFAULT_BOLD
                            UNDERLINE -> textPaint.isUnderlineText = true
                        }
                    }
                }
            }

            override fun onClick(widget: View) {
                data.option.onClick?.invoke()
            }
        }

        spannableText.setSpan(
            urlTagSpan,
            firstIndex,
            firstIndex + data.text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        shift += lastIndex - firstIndex - data.text.length
    }

    return spannableText
}
