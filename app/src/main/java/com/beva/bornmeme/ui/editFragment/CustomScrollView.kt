package com.beva.bornmeme.ui.editFragment

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

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        Timber.e("CustomScrollView onInterceptTouchEvent action=${ev?.action}}")
        if (ev?.action == MotionEvent.ACTION_CANCEL) {
            Timber.e("CustomScrollView onInterceptTouchEvent ACTION_CANCEL}")
            ev.action = MotionEvent.ACTION_MOVE
            return false
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        Timber.e("CustomScrollView onTouchEvent action=${ev?.action}}")

        if (ev?.action == MotionEvent.ACTION_CANCEL) {
            Timber.e("CustomScrollView onTouchEvent ACTION_CANCEL}")
            ev.action = MotionEvent.ACTION_MOVE
            return false
        }
        return super.onTouchEvent(ev)
    }
}