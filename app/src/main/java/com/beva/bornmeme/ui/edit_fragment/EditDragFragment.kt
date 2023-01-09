package com.beva.bornmeme.ui.edit_fragment

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import androidx.collection.arrayMapOf
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MainApplication
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentDragEditBinding
import com.beva.bornmeme.model.UserManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber
import kotlin.math.roundToInt


class EditDragFragment : Fragment() {

    lateinit var binding: FragmentDragEditBinding
    private var uri: Uri? = null

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
        displayText.clearFocus()
        binding.addTextBtn.isEnabled = false

        //reset the coordinate of scrollview
        binding.verticalScrollView.setOnTouchListener { view, event ->

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

        //let the bitmaps width and height scale the view
        arguments?.let { bundle ->
            uri = bundle.getParcelable("uri")
//            Timber.d("uri => $uri")

            val backgroundBitmap = uri?.let {
                StickerUtils.getImage(requireContext(), it)
            }

            if (backgroundBitmap != null) {
                binding.stickerView.setBackground(backgroundBitmap)
                Timber.d("width ${binding.stickerView.width}")
                Timber.d("height ${binding.stickerView.height}")
                Timber.i("bitmap W ${backgroundBitmap.width}")
                Timber.i("bitmap H ${backgroundBitmap.height}")

                val width = backgroundBitmap.width
                var height = backgroundBitmap.height
                val screenWidth = binding.root.context.resources.displayMetrics.widthPixels
                Timber.w("=====================================================")
                Timber.d("screenWidth $screenWidth")
                val deviceDensity = binding.root.context.resources.displayMetrics.density
                Timber.d("density $deviceDensity")
                Timber.d("density ${binding.root.context.resources.displayMetrics.densityDpi}")

                val itemWidth = screenWidth
                Timber.d("itemWidth $itemWidth")

                if (width > height) {
                    Timber.d("width > height")
                    Timber.d("origin width: $width")
                    Timber.d("origin height: $height")
                    height =
                        ((itemWidth.toFloat() / backgroundBitmap.width.toFloat()) * backgroundBitmap.height).roundToInt()

                    Timber.d("after width: $itemWidth")
                    Timber.d("after height: $height")

                } else if (width <= height) {
                    Timber.d("width <= height")
                    Timber.d("origin width: $width")
                    Timber.d("origin height: $height")

                    height = (screenWidth.toFloat() / backgroundBitmap.width.toFloat()
                            * backgroundBitmap.height.toFloat()).roundToInt()
                    Timber.d("after width: $itemWidth")
                    Timber.d("after height: $height")
                }


                val params = ConstraintLayout
                    .LayoutParams(
                        ConstraintLayout
                            .LayoutParams.MATCH_PARENT, height
                    )


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
            binding.stickerView.destroyDrawingCache()
            binding.stickerView.buildDrawingCache()
            val stickerBitmap = binding.stickerView.drawingCache.copy(Bitmap.Config.ARGB_8888, false)
            findNavController().navigate(
                MobileNavigationDirections.navigateToPreviewDialog(
                    stickerBitmap
                )
            )
        }


        binding.dragPublishBtn.setOnClickListener {

            val viewModel = ViewModelProvider(requireActivity())[EditViewModel::class.java]

            if (binding.dragTitleCard.text.toString().isEmpty()) {

                titleSnackbarShow(it, binding.dragTitleCard)

            } else if (binding.dragTextCatalog.text.toString().isEmpty()) {

                tagSnackbarShow(it, binding.dragTextCatalog)

            } else {

                binding.lottiePublishLoading.visibility = View.VISIBLE
                binding.lottiePublishLoading.setAnimation(R.raw.dancing_pallbearers)
                binding.lottiePublishLoading.isEnabled = false

                val document = FirebaseFirestore.getInstance()
                    .collection(getString(R.string.post_collection_text)).document()

                val ref = FirebaseStorage.getInstance().reference
                uri?.let { uri ->
                    ref.child("img_origin/" + document.id + ".jpg")
                        .putFile(uri)
                        .addOnSuccessListener { task ->
                            task.metadata?.reference?.downloadUrl?.addOnSuccessListener { originUri ->
                                //這層的it才會帶到firebase return 的 Uri
//                                Timber.d("origin uri: $it => take it to base url")

            //                            Timber.d("newTag $newTag")
                                val res = listOf(
                                    arrayMapOf("type" to "base", "url" to originUri),
                                    arrayMapOf(
                                        "type" to "text",
                                        "url" to binding.dragTextCatalog.text?.trim().toString()
                                    )
                                )

                                binding.stickerView.destroyDrawingCache()
                                binding.stickerView.buildDrawingCache()
                                val bitmap = binding.stickerView.drawingCache.copy(
                                    Bitmap.Config.ARGB_8888,
                                    false
                                )

                                //saving to gallery and return the path(uri)
                                val newUri = viewModel.getImageUri(activity?.application, bitmap)
                                viewModel.addNewPost(
                                    MainApplication.instance,
                                    newUri,
                                    res,
                                    binding.dragTitleCard.text.toString(),
                                    binding.dragTextCatalog.text.toString(),
                                    bitmap.width,
                                    bitmap.height
                                )

                            }?.addOnSuccessListener {
                                findNavController()
                                    .navigate(MobileNavigationDirections.navigateToHomeFragment())
                            }?.addOnFailureListener { e ->
                                Timber.d("upload uri Error => $e")
                            }
                        }
                }
            }
        }

        //use adapter
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

    }

    private fun chooseSticker(selectedSticker: Bitmap) {
        val reSizeSticker =
            Bitmap.createScaledBitmap(selectedSticker, 220, 200, false)
        binding.stickerView.addSticker(reSizeSticker)
    }
    private fun tagSnackbarShow(it: View, tagText: EditText) {
        val tagSnack =
            Snackbar.make(it, getString(R.string.snack_default_text),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                    .setBackgroundTint(requireContext().getColor(R.color.tr_pink))
                    .setTextColor(requireContext().getColor(R.color.button_balck))
                    .setAction(getString(R.string.snack_default_check)) {
                        tagText.setText(getString(R.string.silly_usual))
                    }.setActionTextColor(requireContext().getColor(R.color.button_balck))

        val snackBarView = tagSnack.view
        val params = snackBarView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL and Gravity.TOP
        snackBarView.layoutParams = params
        tagSnack.show()
    }

    private fun titleSnackbarShow(it: View, titleText: EditText) {
        //Snackbar ani
        val titleSnack =
            Snackbar.make(it, getString(R.string.snack_default_text),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                .setBackgroundTint(requireContext().getColor(R.color.tr_pink))
                .setTextColor(requireContext().getColor(R.color.button_balck))
                .setAction(getString(R.string.snack_default_check)) {
                    titleText.setText(UserManager.user.userName)
                }
                .setActionTextColor(requireContext().getColor(R.color.button_balck))
        val snackBarView = titleSnack.view
        val params = snackBarView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL and Gravity.TOP
        snackBarView.layoutParams = params
        titleSnack.show()
    }

}
