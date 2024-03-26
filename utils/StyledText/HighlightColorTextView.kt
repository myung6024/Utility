import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat

class HighlightColorTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    private var highlightColor = ContextCompat.getColor(context, R.color.red700)
    private var style = STYLE_NONE
    private val textChangeListener: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            removeTextChangedListener(this)
            text = text.toString().setStyledText(StyledTextOption(highlightColor, style = style))
            addTextChangedListener(this)
        }
    }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.HighlightColorTextView)
            highlightColor = typedArray.getColor(R.styleable.HighlightColorTextView_highlightColor, highlightColor)
            style = typedArray.getInteger(R.styleable.HighlightColorTextView_highlightStyle, STYLE_NONE)
            text = text.toString().setStyledText(StyledTextOption(highlightColor, style = style))
            typedArray.recycle()
        }
        addTextChangedListener(textChangeListener)
    }
}
