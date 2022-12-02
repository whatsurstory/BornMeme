package com.beva.bornmeme.ui.editFragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.beva.bornmeme.R
import timber.log.Timber

/**
 * p类,对贴纸、背景进行操作
 */
class StickerPresenter(private val stickerView: StickerView) {

    //    private val defaultBg = Bitmap.createBitmap(
//        10,
//        10,
//        Bitmap.Config.ARGB_8888)
    private val defaultBg = BitmapFactory.decodeResource(stickerView.context.resources, R.drawable.dino)
    private val backgroundDrawer = BackgroundDrawer(stickerView,defaultBg)
    private val stickerDrawers = ArrayList<Drawer>()

    fun onDraw(canvas: Canvas?){
        backgroundDrawer.onDraw(canvas)
        stickerDrawers.forEach {
            it.onDraw(canvas)
        }
    }

    fun onTouchEvent(event: MotionEvent?):Boolean{

        Timber.w("sp event => x: ${event?.x}, y: ${event?.y}")
        //倒叙遍历，后添加的贴纸先消费事件
        for (i in stickerDrawers.size-1 downTo 0) {
            val drawer = stickerDrawers[i]
            if (drawer.onTouchEvent(event)) {
                //当前操作的贴纸将移动到list的尾部，显示在图层最上方
                stickerDrawers.remove(drawer)
                stickerDrawers.add(drawer)

                return true
            }
        }
//        return backgroundDrawer.onTouchEvent(event)
        return false
    }

    fun setOnLongClickListener(long: View.OnLongClickListener): Boolean {
        return true
    }

    fun addSticker(bitmap: Bitmap){
        val drawer = StickerDrawer(stickerView,bitmap)
        stickerDrawers.add(drawer)
        stickerView.invalidate()
    }

    fun clearSticker(){
        stickerDrawers.clear()
        stickerView.invalidate()
    }

    fun deleteSticker(){
        //todo 贴纸删除方案
    }

    fun setBackground(bitmap: Bitmap){
        backgroundDrawer.bitmap = bitmap
        stickerView.invalidate()
    }

    fun clearBackground(){
        backgroundDrawer.bitmap = defaultBg
        stickerView.invalidate()
    }
}