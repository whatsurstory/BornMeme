package com.beva.bornmeme.ui.editFragment

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.net.Uri
import android.util.TypedValue
import kotlin.math.atan
import kotlin.math.sqrt

object StickerUtils {

    fun getImage(context: Context? = null, image: Any): Bitmap? {
        when (image) {
            is String -> {
                return BitmapFactory.decodeFile(image)
            }

            is Uri -> {
                return BitmapFactory.decodeStream(context?.contentResolver?.openInputStream(image))
            }

            is Bitmap -> {
                return image
            }
            else -> {
                return null
            }
        }
    }

    fun dp2px(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics)
    }

    fun px2dp(px: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_PX,
            px,
            Resources.getSystem().displayMetrics)
    }

    fun calculateDistance(point1: PointF, point2: PointF): Float {
        return sqrt((point1.x - point2.x) * (point1.x - point2.x) +
                (point1.y - point2.y) * (point1.y - point2.y))
    }

    /**
     * p1p2为一条线
     * 传进来的参数为手机屏幕坐标系 即 x→  y↓
     * 要将点转化为自然坐标系即 x→ y↑
     */
    fun calculateDegree(p1: PointF, p2: PointF): Double {
        val p = PointF(p1.x - p2.x, p1.y - p2.y)     //过原点射线
//        p.y = -p.y //屏幕坐标系转换成自然坐标系
//        val bevelEdge = sqrt(p.x * p.x + p.y * p.y) //斜边
//        val cos = p.y / bevelEdge
        val tab = p.y/p.x
//        return Math.toDegrees(acos(cos).toDouble())
        return Math.toDegrees(atan(tab).toDouble())
    }
}