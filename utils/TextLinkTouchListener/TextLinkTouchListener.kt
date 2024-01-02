package com.runeanim.mytoyproject.util

import android.annotation.SuppressLint
import android.text.Layout
import android.text.Selection
import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat

class TextLinkTouchListener(
    private val spannable: Spannable,
    private val rippleTarget: View,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) : View.OnTouchListener {

    private var isLongClick = false

    private val gestureDetector = GestureDetectorCompat(
        rippleTarget.context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                onClick()
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                isLongClick = true
                rippleTarget.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                onLongClick()
            }
        }
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val action = event.action
        if (view !is TextView) {
            return false
        }

        if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) && isLongClick.not()) {
            rippleTarget.isPressed = true
        } else if (action == MotionEvent.ACTION_UP) {
            rippleTarget.isPressed = false
        } else if (action == MotionEvent.ACTION_CANCEL) {
            rippleTarget.isPressed = false
            isLongClick = false
        }

        if (action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_DOWN
        ) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= view.totalPaddingLeft
            y -= view.totalPaddingTop
            x += view.scrollX
            y += view.scrollY
            val layout: Layout = view.layout
            val line: Int = layout.getLineForVertical(y)
            val off: Int = layout.getOffsetForHorizontal(line, x.toFloat())
            val link = spannable.getSpans(off, off, ClickableSpan::class.java)
            if (link.isNotEmpty()) {
                if (action == MotionEvent.ACTION_UP) {
                    if (isLongClick) {
                        isLongClick = false
                    } else {
                        link[0].onClick(view)
                        gestureDetector.onTouchEvent(event.apply { setAction(MotionEvent.ACTION_CANCEL) })
                    }
                    return true
                } else {
                    Selection.setSelection(
                        spannable,
                        spannable.getSpanStart(link[0]),
                        spannable.getSpanEnd(link[0])
                    )
                }
            } else {
                if (action == MotionEvent.ACTION_UP && isLongClick) {
                    isLongClick = false
                }
                Selection.removeSelection(spannable)
            }
        }
        gestureDetector.onTouchEvent(event)
        return true
    }
}