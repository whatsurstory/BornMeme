package com.beva.bornmeme.ui.edit_fragment

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView
import timber.log.Timber

class CustomScrollView : ScrollView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        Timber.e("CustomScrollView onInterceptTouchEvent action=${event?.action}}")
        if (event?.action == MotionEvent.ACTION_CANCEL) {
            Timber.e("CustomScrollView onInterceptTouchEvent ACTION_CANCEL}")
            event.action = MotionEvent.ACTION_MOVE
            return false
        }
        return super.onInterceptTouchEvent(event)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Timber.e("CustomScrollView onTouchEvent action=${event?.action}}")

        if (event?.action == MotionEvent.ACTION_CANCEL) {
            Timber.e("CustomScrollView onTouchEvent ACTION_CANCEL}")
            event.action = MotionEvent.ACTION_MOVE
            return false
        }
        return super.onTouchEvent(event)
    }
}