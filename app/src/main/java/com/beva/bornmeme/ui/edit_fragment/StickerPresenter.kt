package com.beva.bornmeme.ui.edit_fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.MotionEvent
import com.beva.bornmeme.R
import timber.log.Timber

/**
 * Presenter
 * method to sticker and background
 */
class StickerPresenter(private val stickerView: StickerView) {

    private val defaultBg =
        BitmapFactory.decodeResource(
            stickerView.context.resources,
            R.drawable.place_holder
        )
    private val backgroundDrawer = BackgroundDrawer(stickerView, defaultBg)
    private val stickerDrawers = ArrayList<Drawer>()

    fun onDraw(canvas: Canvas?) {
        backgroundDrawer.onDraw(canvas)
        stickerDrawers.forEach {
            it.onDraw(canvas)
        }
    }

    fun onTouchEvent(event: MotionEvent?): Boolean {

        Timber.w("sp event => x: ${event?.x}, y: ${event?.y}")

        //reverse position
        for (i in stickerDrawers.size - 1 downTo 0) {
            val drawer = stickerDrawers[i]
            if (drawer.onTouchEvent(event)) {
                stickerDrawers.remove(drawer)
                stickerDrawers.add(drawer)

                return true
            }
        }
//        return backgroundDrawer.onTouchEvent(event)
        return false
    }

    fun addSticker(bitmap: Bitmap) {
        val drawer = StickerDrawer(stickerView, bitmap)
        stickerDrawers.add(drawer)
        stickerView.invalidate()
    }

    fun clearSticker() {
        stickerDrawers.clear()
        stickerView.invalidate()
    }

    fun setBackground(bitmap: Bitmap) {
        backgroundDrawer.bitmap = bitmap
        stickerView.invalidate()
    }

    fun clearBackground() {
        backgroundDrawer.bitmap = defaultBg
        stickerView.invalidate()
    }
}