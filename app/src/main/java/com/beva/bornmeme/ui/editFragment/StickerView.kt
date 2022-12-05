package com.beva.bornmeme.ui.editFragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import timber.log.Timber

class StickerView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attributeSet, defStyle) {

    private val presenter = StickerPresenter(this)

    override fun onDraw(canvas: Canvas?) {
        presenter.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return presenter.onTouchEvent(event)
    }

    fun addSticker(bitmap: Bitmap){
        presenter.addSticker(bitmap)
    }

    fun clearSticker(){
        presenter.clearSticker()
    }

    fun setBackground(bitmap: Bitmap) {
        presenter.setBackground(bitmap)
    }

    fun clearBackground(){
        presenter.clearBackground()
    }
}