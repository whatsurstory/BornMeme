package com.beva.bornmeme.ui.editFragment

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import android.util.FloatMath
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.beva.bornmeme.databinding.FragmentDragEditBinding
import timber.log.Timber
import java.lang.Math.sqrt
import java.lang.StrictMath.sqrt
import kotlin.math.sqrt


class EditDragFragment: Fragment(), View.OnTouchListener {

    lateinit var binding: FragmentDragEditBinding
    // 縮放控制
    private val matrix: Matrix = Matrix()
    private val savedMatrix: Matrix = Matrix()
    // 不同状态的表示：
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private var mode = NONE
    // 定义第一个按下的点，两只接触点的重点，以及出事的两指按下的距离：
    private val startPoint = PointF()
    private var midPoint = PointF()
    private var oriDis = 1f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return binding.root
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentDragEditBinding.inflate(layoutInflater)
        val text = binding.dragEditText
        var isMove = true
//        text.setOnTouchListener(this)
        text.setOnClickListener{
         if (!isMove){
             Timber.d("setOnTouchListener $isMove")
         }
        }

        var startX = 0
        var startY = 0
//
        text.setOnTouchListener { v, event ->
            when(event.action){

                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX.toInt()
                    startY = event.rawY.toInt()
                    isMove = false
                }

                MotionEvent.ACTION_MOVE -> {
                    val endX: Int = event.rawX.toInt()
                    val endY: Int = event.rawY.toInt()

                    val spaceX = endX - startX
                    val spaceY = endY - startY

                    val left = text.left +spaceX
                    val top = text.top +spaceY
                    val right = text.right +left
                    val bottom = text.height +top

                    text.layout(left,top,right, bottom)
                    startX = endX
                    startY = endY

                    if (spaceX >5 || spaceY >5){
                        isMove = true
                    }
                }

//                MotionEvent.ACTION_UP -> {
//                    pUpX= event.x.toInt()
//                    pUpY= event.y.toInt()
//                }
//
//                MotionEvent.ACTION_CANCEL -> {
//
//                }
//
//                else ->{
//
//                }
            }
            return@setOnTouchListener false
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val view = v as ImageView
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                matrix.set(view.imageMatrix)
                savedMatrix.set(matrix)
                startPoint.set(event.x, event.y)
                mode = DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oriDis = distance(event)
                if (oriDis > 10f) {
                    savedMatrix.set(matrix)
                    midPoint = middle(event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> mode = NONE
            MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
//                 是一个手指拖动
                matrix.set(savedMatrix)
                matrix.postTranslate(event.x - startPoint.x, event.y - startPoint.y)
            } else if (mode == ZOOM) {
//                 两个手指滑动
                val newDist = distance(event)
                if (newDist > 10f) {
                    matrix.set(savedMatrix)
                    val scale: Float = newDist / oriDis
                    matrix.postScale(scale, scale, midPoint.x, midPoint.y)
                }
            }
        }
        // 设置ImageView的Matrix
        view.imageMatrix = matrix
        return true
    }

    // 计算两个触摸点之间的距离
    private fun distance(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y)
    }

    // 计算两个触摸点的中点
    private fun middle(event: MotionEvent): PointF {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        return PointF(x / 2, y / 2)
    }
    //https://www.runoob.com/w3cnote/android-tutorial-touchlistener-ontouchevent.html
    //https://ithelp.ithome.com.tw/articles/10190917
    //https://github.com/Aria-Lee/30Day_Challenge/blob/master/DAY04_Scalable_Image(ViewMotionEvent)/app/src/main/java/com/example/fish/day4_scalableimageviewmotionevent/MainActivity.kt
    //https://github.com/slamdon/kotlin-playground/blob/master/4-ScalableImageView/app/src/main/java/devdon/com/scalableimageview/MovableImageView.kt
}