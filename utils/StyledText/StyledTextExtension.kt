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
    val isBold: Boolean = false,
    val isUnderline: Boolean = false,
    val onClick: (() -> Unit)? = null
)

private data class MatchData(
    val range: IntRange,
    val text: String,
    val option: StyledTextOption
)

fun String.setStyledText(
    vararg options: StyledTextOption
): SpannableStringBuilder {
    val spannableText = SpannableStringBuilder(this)
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
                if (data.option.isBold) {
                    textPaint.typeface = Typeface.DEFAULT_BOLD
                }
                if (data.option.isUnderline) {
                    textPaint.isUnderlineText = true
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