package com.beva.bornmeme.ui.editFragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.beva.bornmeme.databinding.FragmentDragEditBinding


class EditDragFragment: Fragment() {

    lateinit var binding: FragmentDragEditBinding
    private var xDown = 0f
    private var yDown = 0f


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
//        val text = binding.dragText
//        var isMove = true

//        text.setOnClickListener{
//         if (!isMove){
//             Timber.d("setOnTouchListener $isMove")
//            }
//        }

//        var startX = 0
//        var startY = 0
//
//        text.setOnTouchListener { v, event ->
//            when(event.action){
//
//                MotionEvent.ACTION_DOWN -> {
//                    startX = event.rawX.toInt()
//                    startY = event.rawY.toInt()
//                    isMove = false
//                }
//
//                MotionEvent.ACTION_MOVE -> {
//
//                    val endX: Int = event.rawX.toInt()
//                    val endY: Int = event.rawY.toInt()
//
//                    val spaceX = endX - startX
//                    val spaceY = endY - startY
//
//                    val left = text.left + spaceX
//                    val top = text.top + spaceY
//                    val right = text.right + spaceX
//                    val bottom = text.bottom + spaceY
//
//                    text.layout(left,top,right, bottom)
//                    startX = endX
//                    startY = endY
//
//                    if (spaceX > 5 || spaceY > 5){
//                        isMove = true
//                    }
//                }
//
//                MotionEvent.ACTION_UP -> {
//
//                }
//
//                MotionEvent.ACTION_CANCEL -> {
//
//                }
//
//                else ->{
//
//                }
//            }
//            return@setOnTouchListener false
//        }


        binding.stickerImg.setOnTouchListener(OnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    xDown = event.x
                    yDown = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val moveX: Float = event.x
                    val moveY: Float = event.y
                    val distanceX: Float = moveX - xDown
                    val distanceY: Float = moveY - yDown
                    binding.stickerImg.x = binding.stickerImg.x + distanceX
                    binding.stickerImg.y = binding.stickerImg.y + distanceY
                }
            }
            true
        })
        binding.dragText.setOnTouchListener(OnTouchListener { v, event ->
            // TODO Auto-generated method stub
            drag(event, v)
            false
        })

    }

    private fun drag(event: MotionEvent?, v: View?) {
        val params = v?.layoutParams as ConstraintLayout.LayoutParams

        when (event!!.action) {
            MotionEvent.ACTION_MOVE -> {
                params.topMargin = event.rawY.toInt() - v.height
                params.leftMargin = event.rawX.toInt() - v.width / 2
                v.layoutParams = params
            }
            MotionEvent.ACTION_UP -> {
                params.topMargin = event.rawY.toInt() - v.height
                params.leftMargin = event.rawX.toInt() - v.width / 2
                v.layoutParams = params
            }
            MotionEvent.ACTION_DOWN -> {
                v.layoutParams = params
            }
        }
    }
}
