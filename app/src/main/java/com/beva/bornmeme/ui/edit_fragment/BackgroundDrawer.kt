package com.beva.bornmeme.ui.edit_fragment

import android.graphics.Bitmap
import android.graphics.Matrix

/**
 * 贴纸背景
 */
class BackgroundDrawer(stickerView: StickerView, bitmap: Bitmap): Drawer(stickerView,bitmap)  {

    override fun onInit() {
        //let the background view gravity center
        val ratio = stickerView.width.toFloat() / bitmap.width
        matrix.setScale(ratio, ratio, bitmap.width / 2f, bitmap.height / 2f)
        matrix.getValues(array)
        val dx = (bitmap.width - bitmap.width * array[Matrix.MSCALE_X]) / 2
        val dy = (bitmap.height - bitmap.height * array[Matrix.MSCALE_X]) / 2
        matrix.postTranslate(-1 * dx, -1 * dy)
        matrix.postTranslate(0f, (stickerView.height - bitmap.height * array[Matrix.MSCALE_X]) / 2)
    }

    override fun allowRotation(): Boolean  = false
    override fun allowScale(): Boolean = false
}