package com.beva.bornmeme.ui.editFragment


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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
    private lateinit var uri: Uri
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
            uri = bundle.getParcelable("uri")!!
            Timber.d("From Album uri => $uri")
        }
        getLayoutPrams()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(EditViewModel::class.java)
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

        (tagText.parent.parent as ViewGroup)
            .setBackgroundColor(Color.parseColor("#EADDDB"))
        (titleText.parent.parent as ViewGroup)
            .setBackgroundColor(Color.parseColor("#EADDDB"))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //to preview
        binding.previewBtn.setOnClickListener {
            Timber.d("onClick Preview")
            upperText.clearFocus()
            bottomText.clearFocus()
            if (upperText.text.isNullOrEmpty() || bottomText.text.isNullOrEmpty()) {
                viewModel.showContentEmptySnackBar(it)
            } else {
//                val baseBitmap = getBitmapByUri(uri)
                binding.originPhoto.buildDrawingCache()
                val baseBitmap = binding.originPhoto.drawingCache
                upperText.buildDrawingCache()
                val upperBitmap = upperText.drawingCache
                bottomText.buildDrawingCache()
                val bottomBitmap = bottomText.drawingCache
                val previewBitmap = viewModel
                    .mergeBitmap(baseBitmap, upperBitmap, bottomBitmap)

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
            Timber.d("onClick publish")
            upperText.clearFocus()
            bottomText.clearFocus()
            if (upperText.text.isNullOrEmpty() || bottomText.text.isNullOrEmpty()) {
                viewModel.showContentEmptySnackBar(it)
            } else if (titleText.text.trim().isEmpty()) {
                viewModel.titleSnackbarShow(it, titleText)
            } else if (tagText.text.trim().isEmpty()) {
                viewModel.tagSnackbarShow(it, tagText)
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

                            val res = listOf(
                                arrayMapOf("type" to "base", "url" to it),
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
                            val newUri = viewModel.getImageUri(activity?.application, publishBitmap)
                            viewModel.addNewPost(
                                newUri,
                                res,
                                titleText.text.toString(),
                                tagText.text.toString(),
                                publishBitmap.width,
                                publishBitmap.height
                            )
                            findNavController().navigate(MobileNavigationDirections.navigateToHomeFragment())

                        }?.addOnFailureListener {
                            Timber.d("upload uri Error => $it")
                        }
                    }.addOnFailureListener {
                        Timber.d("TaskSnapshot Error => $it")
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
            requireContext().contentResolver.openInputStream(uri),
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

}