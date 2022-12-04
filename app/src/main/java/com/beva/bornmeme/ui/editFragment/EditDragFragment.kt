package com.beva.bornmeme.ui.editFragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.collection.arrayMapOf
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MainViewModel
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentDragEditBinding
import com.beva.bornmeme.model.UserManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.common.base.Strings.isNullOrEmpty
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber
import kotlin.math.roundToInt
import kotlin.properties.Delegates


class EditDragFragment: Fragment() {

    lateinit var binding: FragmentDragEditBinding
//    private var xDown = 0f
//    private var yDown = 0f
    private lateinit var uri: Uri
    private val fireStore = FirebaseFirestore.getInstance().collection("Posts")
    private val document = fireStore.document()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentDragEditBinding.inflate(layoutInflater)

        val displayText = binding.editTextBox
//        displayText.requestFocus()
        displayText.clearFocus()
        binding.addTextBtn.isEnabled = false


        //reset the coordinate of scrollview
        binding.verticalScrollView.setOnTouchListener { view, event ->

//            Timber.i("stickerView event => x: ${binding.stickerView.x}, y: ${binding.stickerView.y}")
//            Timber.i("stickerView H -> ${binding.stickerView.height}")
//            Timber.d("screen H -> ${binding.root.context.resources.displayMetrics.widthPixels}")

            val offset = binding.stickerView.y
            Timber.e("offset => $offset")
            Timber.d("scrollview event => x: ${event?.x}, y: ${event?.y}")
            Timber.e("event.y - offset => ${event.y - offset}")
            Timber.d("scrollY -> ${binding.verticalScrollView.scrollY}")
            Timber.e("event.y - offset + binding.verticalScrollView.scrollY => ${event.y - offset + binding.verticalScrollView.scrollY}")


            val newEvent = MotionEvent.obtain(
                event.downTime,
                event.eventTime,
                event.action,
                event.x,
                event.y - offset + binding.verticalScrollView.scrollY,
                event.metaState
            )
            return@setOnTouchListener binding.stickerView.onTouchEvent(newEvent)
        }

        //let the bitmap's width and height scale the view
        arguments?.let { bundle ->
            uri = bundle.getParcelable("uri")!!
            Timber.d("uri => $uri")
            val bitmap = StickerUtils.getImage(requireContext(), uri)
            if (bitmap != null) {
                binding.stickerView.setBackground(bitmap)
                Timber.d("width ${binding.stickerView.width}")
                Timber.d("height ${binding.stickerView.height}")
                Timber.i("bitmap W ${bitmap.width}")
                Timber.i("bitmap H ${bitmap.height}")

                val width = bitmap.width
                var height = bitmap.height
                val screenWidth = binding.root.context.resources.displayMetrics.widthPixels
                Timber.w("=====================================================")
                Timber.d("screenWidth $screenWidth")
                val deviceDensity = binding.root.context.resources.displayMetrics.density
                Timber.d("density $deviceDensity")
                Timber.d("density ${binding.root.context.resources.displayMetrics.densityDpi}")

                val itemWidth = screenWidth
                Timber.d("itemWidth ${itemWidth}")


                if (width > height) {
                    Timber.d("寬大於高")
                    Timber.d("origin width: $width")
                    Timber.d("origin height: $height")
                    height = ((itemWidth.toFloat() / bitmap.width.toFloat()) * bitmap.height).roundToInt()

                    Timber.d("after width: $itemWidth")
                    Timber.d("after height: $height")

                } else if (width <= height) {
                    Timber.d("寬小於高")
                    Timber.d("origin width: $width")
                    Timber.d("origin height: $height")
//                    if (height > itemWidth * 1.3){
//                        height = (itemWidth * 1.3).roundToInt()
//                    }

                    height = (screenWidth.toFloat() / bitmap.width.toFloat()
                            * bitmap.height.toFloat()).roundToInt()
                    Timber.d("after width: $itemWidth")
                    Timber.d("after height: $height")
                }


                val params = ConstraintLayout
                    .LayoutParams(ConstraintLayout
                        .LayoutParams.MATCH_PARENT, height)


                binding.stickerView.layoutParams = params
                binding.stickerView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topToBottom = binding.addTextBtn.id
                    bottomToTop = binding.dragTitleCard.id
                    //add other constraints if needed
                }

            }
        }

        displayText.doOnTextChanged { text, start, count, after ->
            // action which will be invoked when the text is changing
            binding.addTextBtn.isEnabled = !text?.trim().isNullOrEmpty()
        }

        binding.addTextBtn.setOnClickListener {
            displayText.destroyDrawingCache()
            displayText.buildDrawingCache()
            val bitmap = displayText.drawingCache.copy(Bitmap.Config.ARGB_8888, false)
            binding.stickerView.addSticker(bitmap)
            displayText.setText("")
        }

        binding.clearButton.setOnClickListener {
            binding.stickerView.clearSticker()
        }

        binding.dragPreviewBtn.setOnClickListener {
            Timber.d("displayText.text?.trim() ${binding.dragTextCatalog.text?.trim()}")
            binding.stickerView.destroyDrawingCache()
            binding.stickerView.buildDrawingCache()
            val bitmap = binding.stickerView.drawingCache.copy(Bitmap.Config.ARGB_8888, false)
            findNavController().navigate(
                MobileNavigationDirections.navigateToPreviewDialog(
                    bitmap
                )
            )
        }

        binding.dragPublishBtn.setOnClickListener {
            if (binding.dragTitleCard.text.toString().isEmpty()) {
                //Snackbar ani
                val titleSnack = Snackbar.make(it,"資料未完成將填入預設值，不客氣", Snackbar.LENGTH_INDEFINITE)
                titleSnack.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
                titleSnack.setBackgroundTint(Color.parseColor("#EADDDB"))
                titleSnack.setTextColor(Color.parseColor("#181A19"))
                titleSnack.setAction("感恩的心") {
                    binding.dragTitleCard.setText(UserManager.user.userName)
                }
                titleSnack.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.tr_black))
                val snackBarView = titleSnack.view
                val params = snackBarView.layoutParams as FrameLayout.LayoutParams
                params.gravity =  Gravity.CENTER_HORIZONTAL and Gravity.TOP
                snackBarView.layoutParams = params
                titleSnack.show()

            } else if (binding.dragTextCatalog.text.toString().isEmpty()) {
                val tagSnack =
                    Snackbar.make( it,"資料未完成將填入預設值，不客氣", Snackbar.LENGTH_INDEFINITE)
                        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                        .setBackgroundTint(Color.parseColor("#EADDDB"))
                        .setTextColor(Color.parseColor("#181A19"))
                        .setAction("感謝有你") {
                            binding.dragTextCatalog.setText("傻逼日常")
                        }
                        .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.tr_black))
                val snackBarView = tagSnack.view
                val params = snackBarView.layoutParams as FrameLayout.LayoutParams
                params.gravity =  Gravity.CENTER_HORIZONTAL and Gravity.TOP
                snackBarView.layoutParams = params
                tagSnack.show()
            } else {
                binding.lottiePublishLoading.visibility = View.VISIBLE
                binding.lottiePublishLoading.setAnimation(R.raw.dancing_pallbearers)

                val ref = FirebaseStorage.getInstance().reference
                ref.child("img_origin/" + document.id + ".jpg")
                    .putFile(uri)
                    .addOnSuccessListener {
                        it.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                            //這層的it才會帶到firebase return 的 Uri
                            Timber.d("origin uri: $it => take it to base url")

//                            Timber.d("newTag $newTag")
                            val res = listOf(
                                arrayMapOf("type" to "base", "url" to it),
                                arrayMapOf(
                                    "type" to "text",
                                    "url" to binding.dragTextCatalog.text?.trim().toString()
                                )
                            )

                            binding.stickerView.destroyDrawingCache()
                            binding.stickerView.buildDrawingCache()
                            val bitmap = binding.stickerView.drawingCache.copy(Bitmap.Config.ARGB_8888, false)

                            //saving to gallery and return the path(uri)
                            val viewModel = ViewModelProvider(requireActivity())[EditViewModel::class.java]
                            val newUri = viewModel.getImageUri(activity?.application, bitmap)
                            viewModel.addNewPost(
                                newUri,
                                res,
                                binding.dragTitleCard.text.toString(),
                                binding.dragTextCatalog.text.toString(),
                                bitmap.width,
                                bitmap.height)
                            findNavController().navigate(MobileNavigationDirections.navigateToHomeFragment())

                        }?.addOnFailureListener {
                            Timber.d("upload uri Error => $it")
                        }

                    }
            }
        }

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
        binding.meme28454.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeAngryTrollFace.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeVector.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeTransparent.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeRageFace.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeMeGusta.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeRageFace.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeHappyDerpina.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeDerpina.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeFunnyFaces.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeGirl.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeForeverAlone.setOnClickListener {
            chooseSticker(it.background.toBitmap())
        }
        binding.memeTrollFace.setOnClickListener {
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
        val reSizeSticker = Bitmap.createScaledBitmap(selectedSticker, 220, 200, false)
        binding.stickerView.addSticker(reSizeSticker)
    }

}
