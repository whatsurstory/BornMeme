package com.beva.bornmeme.ui.edit_fragment


import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import androidx.collection.arrayMapOf
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MainApplication
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentEditFixmodeBinding
import com.beva.bornmeme.model.UserManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber


class EditFragment : Fragment() {

    private lateinit var binding: FragmentEditFixmodeBinding
    private var uri: Uri? = null
    private lateinit var upperText: AppCompatEditText
    private lateinit var bottomText: AppCompatEditText
    private lateinit var tagText: EditText
    private lateinit var titleText: EditText
    private val fireStore = FirebaseFirestore.getInstance().collection("Posts")
    private val document = fireStore.document()
    private lateinit var viewModel: EditViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentEditFixmodeBinding.inflate(layoutInflater)

        arguments?.let { bundle ->
            uri = bundle.getParcelable("uri")
//            Timber.d("From Album uri => $uri")
        }
        getLayoutPrams()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[EditViewModel::class.java]
        upperText = binding.upperText
        bottomText = binding.bottomText
        tagText = binding.editTextCatalog
        titleText = binding.editTextTitle

        tagText.doOnTextChanged { text, _, _, _ ->
            // action which will be invoked when the text is changing
            binding.catalogCard.error =
                if (text.isNullOrEmpty()) getString(R.string.input_yet) else null
        }

        titleText.doOnTextChanged { text, _, _, _ ->
            // action which will be invoked when the text is changing
            binding.titleCard.error =
                if (text.isNullOrEmpty()) getString(R.string.input_yet) else null
        }

        context?.let {
            (tagText.parent.parent as ViewGroup)
                .setBackgroundColor(it.getColor(R.color.tr_pink))
        }
        context?.let {
            (titleText.parent.parent as ViewGroup)
                .setBackgroundColor(it.getColor(R.color.tr_pink))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //to preview
        binding.previewBtn.setOnClickListener {
//            Timber.d("onClick Preview")
            upperText.clearFocus()
            bottomText.clearFocus()
            if (upperText.text.isNullOrEmpty() || bottomText.text.isNullOrEmpty()) {
                showContentEmptySnackBar(it)
            } else {
//                val baseBitmap = getBitmapByUri(uri)
                binding.originPhoto.buildDrawingCache()
                val baseBitmap = binding.originPhoto.drawingCache
                upperText.buildDrawingCache()
                val upperBitmap = upperText.drawingCache
                bottomText.buildDrawingCache()
                val bottomBitmap = bottomText.drawingCache
                val previewBitmap =
                    viewModel.mergeBitmap(baseBitmap, upperBitmap, bottomBitmap)

                upperText.destroyDrawingCache()
                bottomText.destroyDrawingCache()
                binding.originPhoto.destroyDrawingCache()

                findNavController().navigate(
                    MobileNavigationDirections.navigateToPreviewDialog(
                        previewBitmap
                    )
                )
            }
        }

        //to publish
        binding.publishBtn.setOnClickListener {
//            Timber.d("onClick publish")
            upperText.clearFocus()
            bottomText.clearFocus()
            if (upperText.text.isNullOrEmpty() || bottomText.text.isNullOrEmpty()) {
                showContentEmptySnackBar(it)
            } else if (titleText.text.trim().isEmpty()) {
                titleSnackbarShow(it, titleText)
            } else if (tagText.text.trim().isEmpty()) {
                tagSnackbarShow(it, tagText)
            } else {

                binding.lottiePublishLoading.visibility = View.VISIBLE
                binding.lottiePublishLoading.setAnimation(R.raw.dancing_pallbearers)
                binding.lottiePublishLoading.isEnabled = false

                val ref = FirebaseStorage.getInstance().reference
                uri?.let { uri ->
                    ref.child("img_origin/" + document.id + ".jpg")
                        .putFile(uri)
                        .addOnSuccessListener { task ->
                            task.metadata?.reference?.downloadUrl?.addOnSuccessListener { originUri ->
                                //這層的it才會帶到firebase return 的 Uri
//                                Timber.d("origin uri: $it => take it to base url")

                                val res = listOf(
                                    arrayMapOf("type" to "base", "url" to originUri),
                                    arrayMapOf(
                                        "type" to "text",
                                        "url" to "${upperText.text!!.trim()}\n${bottomText.text!!.trim()}"
                                    )
                                )

                                binding.originPhoto.buildDrawingCache()
                                val baseBitmap = binding.originPhoto.drawingCache
                                upperText.buildDrawingCache()
                                val upperBitmap = upperText.drawingCache
                                bottomText.buildDrawingCache()
                                val bottomBitmap = bottomText.drawingCache

                                val publishBitmap = viewModel.mergeBitmap(
                                    baseBitmap,
                                    upperBitmap,
                                    bottomBitmap
                                )

                                //saving to gallery and return the path(uri)
                                val newUri =
                                    viewModel.getImageUri(activity?.application, publishBitmap)
                                viewModel.addNewPost(
                                    MainApplication.instance,
                                    newUri,
                                    res,
                                    titleText.text.toString(),
                                    tagText.text.toString(),
                                    publishBitmap.width,
                                    publishBitmap.height
                                )

                            }?.addOnSuccessListener {
                                findNavController()
                                    .navigate(MobileNavigationDirections.navigateToHomeFragment())
                            }?.addOnFailureListener { e ->
                                Timber.d("upload uri Error => $e")
                            }
                        }.addOnFailureListener { e ->
                            Timber.d("TaskSnapshot Error => $e")
                        }
                }
            }
        }
    }


//    private fun getBitmapByUri(bitmapUri: Uri): Bitmap {
//        return BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(bitmapUri))
//    }

    private fun getLayoutPrams() {

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        BitmapFactory.decodeStream(
            uri?.let {
                requireContext().contentResolver.openInputStream(it)
                     },
            null,
            options
        )

        //Gallery Image origin Width &Height
        val oriWidth = options.outWidth
        val oriHeight = options.outHeight
        Timber.w("oriWidth => $oriWidth")
        Timber.w("oriHeight => $oriHeight")

        //Screen ViewWidth & Height
        val screenWidth = resources.displayMetrics.widthPixels
//        val screenHeight = resources.displayMetrics.heightPixels

        //After combine Image Height
        val height = (screenWidth.toFloat() / oriWidth.toFloat() * oriHeight.toFloat()).toInt()

        Timber.d("final width => $screenWidth")
        Timber.d("final height => $height")

        val layoutParam = ConstraintLayout.LayoutParams(
            screenWidth,
            height
        )

        //let new height and width assign as constraint layout parameter
        binding.originPhoto.layoutParams = layoutParam
        binding.originPhoto.scaleType = ImageView.ScaleType.FIT_CENTER
        binding.originPhoto.setImageURI(uri)

    }

    private fun showContentEmptySnackBar(it: View) {
        val contentText = Snackbar.make(
            it, getString(R.string.edit_no_input),
            Snackbar.LENGTH_LONG
        )
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
            .setBackgroundTint(requireContext().getColor(R.color.tr_pink))
            .setTextColor(requireContext().getColor(R.color.button_balck))
        val snackBarView = contentText.view
        val params = snackBarView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL and Gravity.TOP
        snackBarView.layoutParams = params
        contentText.show()
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