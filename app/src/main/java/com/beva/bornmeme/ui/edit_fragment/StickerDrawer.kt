package com.beva.bornmeme.ui.edit_fragment

import android.graphics.Bitmap

/**
 * setting the class of sticker initialize position
 */
class StickerDrawer(
    stickerView: StickerView,
    bitmap: Bitmap
): Drawer(stickerView,bitmap) {

    override fun onInit() {
//        matrix.setTranslate(((stickerView.measuredWidth-bitmap.width)/2).toFloat(),
//            ((stickerView.measuredHeight-bitmap.height)/2).toFloat())
        matrix.setTranslate(100f,100f)
    }
}