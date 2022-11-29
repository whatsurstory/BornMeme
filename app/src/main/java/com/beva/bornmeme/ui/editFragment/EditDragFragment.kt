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
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentDragEditBinding
import timber.log.Timber
import kotlin.properties.Delegates


class EditDragFragment: Fragment() {

    lateinit var binding: FragmentDragEditBinding
//    private var xDown = 0f
//    private var yDown = 0f
    private lateinit var uri: Uri
    private var selectedSticker by Delegates.notNull<Int>()

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

        val displayText = binding.editTextBox
        //let the text won't have underline
        displayText.requestFocus()
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
            Timber.d("uri => $uri")
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
                binding.addTextBtn.isEnabled = true
            }
        }

        binding.addTextBtn.setOnClickListener {
            displayText.destroyDrawingCache()
            displayText.buildDrawingCache()
            val bitmap = displayText.drawingCache.copy(Bitmap.Config.ARGB_8888, false)
            binding.stickerView.addSticker(bitmap)
        }

        binding.dragPreviewBtn.setOnClickListener {  }
        binding.dragPublishBtn.setOnClickListener {  }

        //adding default sticker
        binding.memeAwkwardLookMonkeyPuppet.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeBabyYodaDrinkingSoup.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeCatBeingYelledAt.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeCryingCat.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeCryingKimKardashian.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeCryingMichaelJordan.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeDogeDog.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeFacepalm.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeHandsomeSquidward.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeHideThePainHarold.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeHomerSimpsonBushes.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeIsThisAPigeon.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeKermitNotMyBusiness.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeNortonSmirking.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memePatrickIHave3Dollars.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeLeonardoDicaprioLaughing.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memePoliteCat.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeRalphWiggumDivingThroughWindow.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeRollSafe.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeSadFrog.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeSaltBae.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeSurprisedShockedPikachu.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeThisIsFineDog.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeWomanYelling.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeYaoMing.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }

//        binding.addStickerBtn.setOnClickListener {
//            val sticker = BitmapFactory.decodeResource(resources, R.drawable.heart)
//            val reSizeSticker = Bitmap.createScaledBitmap(sticker, 100, 100, false)
//            binding.stickerView.addSticker(reSizeSticker)
//        }
    }

    private fun chooseSticker(selectedSticker: Bitmap) {
//        val sticker = BitmapFactory.decodeResource(resources, selectedSticker)
        val reSizeSticker = Bitmap.createScaledBitmap(selectedSticker, 100, 100, false)
        binding.stickerView.addSticker(reSizeSticker)
    }

}
