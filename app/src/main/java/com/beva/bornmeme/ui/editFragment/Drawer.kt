package com.beva.bornmeme.ui.editFragment

import android.animation.ValueAnimator
import android.graphics.*
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.tan

/**
 * 贴纸、背景图抽象成Drawer
 * 实现出界回弹、手势的功能
 * https://ithelp.ithome.com.tw/articles/10203014
 * https://ithelp.ithome.com.tw/articles/10190917
 */
abstract class Drawer(val stickerView: StickerView, var bitmap: Bitmap) {

    val TAG = javaClass.simpleName
    val matrix = Matrix()
    private val paint = Paint()
    private val rectF = RectF()
    val array = FloatArray(9)

    private var point1 = PointF(0f, 0f)
    private var point2 = PointF(0f, 0f)
    private var distance = 0f
    //用于控制双指缩放或旋转时禁止移动
    private var canMove = true
    //是否消费单指或双指时间，用于不同贴纸间的事件拦截处理
    private var consumeTapOne = false
    private var consumeTapTwin = false

    init {
        stickerView.post {
            onInit()
            stickerView.invalidate()
        }
    }

    open fun onDraw(canvas: Canvas?) {
        canvas?.drawBitmap(bitmap, matrix, paint)
    }

    open fun onTouchEvent(event: MotionEvent?): Boolean {
        //这里使用actionMasked，只有actionMask才能检测到多指按下
        when (event?.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                if (event.pointerCount == 1) {
                    consumeTapOne = checkTouch(event.x,event.y)
                    if(consumeTapOne){
                        point1.x = event.x
                        point1.y = event.y
                        return true
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                //单指移动背景
                if (event.pointerCount == 1 && canMove && consumeTapOne) {
                    Timber.d("event => x: ${event.x}, y: ${event.y}")
                    Timber.d("point1 => x: ${point1.x}, y: ${point1.y}")
                    val dx = event.x - point1.x
                    val dy = event.y - point1.y
                    Timber.d(" => dx: ${dx}, dy: ${dy}")
                    matrix.postTranslate(dx, dy)
                    point1.x = event.x
                    point1.y = event.y
                    mapRect()
                    stickerView.invalidate()
                    return true
                }

                if (event.pointerCount == 2 && consumeTapTwin) {
                    canMove = false
                    val p1 = PointF(event.getX(0), event.getY(0))
                    val p2 = PointF(event.getX(1), event.getY(1))
                    val newDistance = StickerUtils.calculateDistance(p1, p2)
                    val newRatio = newDistance / distance
                    matrix.getValues(array)
                    val x = array[Matrix.MTRANS_X]
                    val y = array[Matrix.MTRANS_Y]
                    val oldRatio = array[Matrix.MSCALE_X]
                    if(allowScale()){
                        //双指缩放
                        matrix.postScale(newRatio,
                            newRatio,
                            x + bitmap.width * oldRatio / 2,
                            y + bitmap.height * oldRatio / 2)
//                    }
                        distance = newDistance
                    }
                    if(allowRotation()){
                        //双指旋转
                        val d1 = StickerUtils.calculateDegree(point1, point2)
                        val d2 = StickerUtils.calculateDegree(p1, p2)
                        matrix.postRotate((d2 - d1).toFloat(),
                            (rectF.left + rectF.right) / 2,
                            (rectF.top + rectF.bottom) / 2)
                        point1 = p1
                        point2 = p2
                    }
                    mapRect()
                    stickerView.invalidate()
                    return true
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {

                if (event.pointerCount == 2) {
                    point1 = PointF(event.getX(0), event.getY(0))
                    point2 = PointF(event.getX(1), event.getY(1))
                    distance = StickerUtils.calculateDistance(point1, point2)
                    consumeTapTwin = checkTouch(point1.x,point1.y) && checkTouch(point2.x,point2.y)
                    if(consumeTapTwin)
                        return true
                }
            }

            MotionEvent.ACTION_UP -> {
                matrix.getValues(array)
                mapRect()
                val x = rectF.left
                val y = rectF.top
                rebound(x, y)
                canMove = true
                if(consumeTapTwin || consumeTapOne)
                    return true
            }
        }
        return false
    }

    /**
     * matrix映射成rectF
     */
    fun mapRect(): RectF {
        matrix.getValues(array)
        val width = abs(bitmap.width * array[Matrix.MSCALE_X])
        val height = abs(bitmap.height * array[Matrix.MSCALE_X])
        val angle = getRotationDegree()
        var radian = Math.toRadians(getRotationDegree())
        val line = abs(width * tan(abs(radian)).toFloat())  //是那条短的变

        when (angle) {
            in -90.0..0.0 -> {
                rectF.left = array[Matrix.MTRANS_X] - line
                rectF.right = array[Matrix.MTRANS_X] + width
                rectF.top = array[Matrix.MTRANS_Y]
                rectF.bottom = array[Matrix.MTRANS_Y] + height + line
            }
            in 0.0..90.0 -> {
                rectF.left = array[Matrix.MTRANS_X]
                rectF.right = array[Matrix.MTRANS_X] + line + width
                rectF.top = array[Matrix.MTRANS_Y] - line
                rectF.bottom = array[Matrix.MTRANS_Y] + height
            }
            in -180.0..-90.0 -> {
                rectF.left = array[Matrix.MTRANS_X] - height - line
                rectF.right = array[Matrix.MTRANS_X]
                rectF.top = array[Matrix.MTRANS_Y] - height
                rectF.bottom = array[Matrix.MTRANS_Y] + line
            }
            in 90.0..180.0 -> {
                rectF.left = array[Matrix.MTRANS_X] - width
                rectF.right = array[Matrix.MTRANS_X] + line
                rectF.top = array[Matrix.MTRANS_Y] - height - line
                rectF.bottom = array[Matrix.MTRANS_Y]
            }
        }
        return rectF
    }

    /**
     * 当图片超出view的边界时，回弹到中心
     */
    private fun rebound(x: Float, y: Float) {
        val vW = stickerView.width
        val vH = stickerView.height
        mapRect()
        if (rectF.top < 0 || rectF.right > vW || rectF.left < 0 || rectF.bottom > vH) {
            val anim = ValueAnimator.ofFloat(1f, 0f)
            anim.interpolator = AccelerateDecelerateInterpolator()
            anim.addUpdateListener {
                mapRect()
                matrix.getValues(array)
                val value = it.animatedValue as Float
                if (rectF.top < 0 && rectF.bottom < vH) {
                    matrix.postTranslate(0f, y * value - rectF.top)
                }
                if (rectF.bottom > vH && rectF.top > 0) {
                    matrix.postTranslate(0f, (y + rectF.height() - vH) * value + vH - rectF.bottom)
                }
                if (rectF.left < 0 && rectF.right < vW) {
                    matrix.postTranslate(x * value - rectF.left, 0f)
                }
                if (rectF.right > vW && rectF.left > 0) {
                    matrix.postTranslate((x + rectF.width() - vW) * value + vW - rectF.right, 0f)
                }
                stickerView.invalidate()
            }
            anim.start()
        }
    }

    /**
     * 获取图片旋转角度
     */
    private fun getRotationDegree(): Double {
        return (atan2(array[Matrix.MSKEW_X], array[Matrix.MSCALE_X]) * (180 / Math.PI))
    }

    //默认放置在view中间、与view等宽，等比缩放
    /**
     * 初始化图片位置、缩放等
     */
    open fun onInit() {
        //缩放图片与view等宽，将图片移动到view中间
        val ratio = stickerView.width.toFloat() / bitmap.width
        matrix.setScale(ratio, ratio, bitmap.width / 2f, bitmap.height / 2f)
        matrix.getValues(array)
        val dx = (bitmap.width - bitmap.width * array[Matrix.MSCALE_X]) / 2
        val dy = (bitmap.height - bitmap.height * array[Matrix.MSCALE_X]) / 2
        matrix.postTranslate(-1 * dx, -1 * dy)
        matrix.postTranslate(0f,
            (stickerView.height - bitmap.height * array[Matrix.MSCALE_X]) / 2)
    }

    /**
     * 判断手指是否触碰到对应贴纸
     */
    private fun checkTouch(x: Float,y: Float): Boolean {
        mapRect()
        if (x in rectF.left-10..rectF.right+10 && y in rectF.top-10..rectF.bottom+10)
            return true
        return false
    }

    open fun allowRotation() = true
    open fun allowScale() = true
}