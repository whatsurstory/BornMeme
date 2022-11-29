package com.beva.bornmeme.ui.editFragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentDragEditBinding
import timber.log.Timber


class EditDragFragment: Fragment() {

    lateinit var binding: FragmentDragEditBinding
    private var xDown = 0f
    private var yDown = 0f
    private lateinit var uri:Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentDragEditBinding.inflate(layoutInflater)
//        val input = binding.inputText
        val displayText = binding.editTextBox
        binding.addTextBtn.isEnabled = false
//        binding.stickerImg.setOnTouchListener(OnTouchListener { v, event ->
//            when (event.actionMasked) {
//                MotionEvent.ACTION_DOWN -> {
//                    xDown = event.x
//                    yDown = event.y
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    val moveX: Float = event.x
//                    val moveY: Float = event.y
//                    val distanceX: Float = moveX - xDown
//                    val distanceY: Float = moveY - yDown
//                    binding.stickerImg.x = binding.stickerImg.x + distanceX
//                    binding.stickerImg.y = binding.stickerImg.y + distanceY
//                }
//            }
//            true
//        })
        arguments?.let { bundle ->
            uri = bundle.getParcelable("uri")!!
            Timber.d("From Album uri => $uri")
            val bitmap = StickerUtils.getImage(requireContext(), uri)
            if (bitmap != null) {
                binding.stickerView.setBackground(bitmap)
            }
        }

        displayText.doOnTextChanged { text, start, count, after ->
            // action which will be invoked when the text is changing
            if (text.isNullOrEmpty()) {
                Timber.d("text $text")
            } else {
//                binding.inputText.text = text
                binding.addTextBtn.isEnabled = true
            }
        }

        binding.addStickerBtn.setOnClickListener {
            val sticker = BitmapFactory.decodeResource(resources, R.drawable.heart)
            val reSizeSticker = Bitmap.createScaledBitmap(sticker, 100, 100, false)
            binding.stickerView.addSticker(reSizeSticker)
        }

        binding.addTextBtn.setOnClickListener {
            displayText.destroyDrawingCache()
            displayText.buildDrawingCache()
            val bitmap = displayText.drawingCache.copy(Bitmap.Config.ARGB_8888, false)

            binding.stickerView.addSticker(bitmap)

        }
    }

    private fun createNewTextView(text: String): TextView? {
        val lparams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        val textView = TextView(requireContext())
        textView.layoutParams = lparams
        textView.text = text
        return textView
    }
}
