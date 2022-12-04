package com.beva.bornmeme.ui.editFragment


import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.collection.arrayMapOf
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import androidx.core.view.setPadding
import androidx.core.widget.addTextChangedListener
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
//TODO: REPOSITORY to be complete

    private lateinit var binding: FragmentEditFixmodeBinding
    private lateinit var uri: Uri
    private lateinit var upperText: AppCompatEditText
    private lateinit var bottomText: AppCompatEditText
    private lateinit var tag: EditText
    private lateinit var title:EditText
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
        tag = binding.editTextCatalog
        title = binding.editTextTitle

        tag.doOnTextChanged { text, start, count, after ->
            // action which will be invoked when the text is changing
            if (text.isNullOrEmpty()) {
                binding.catalogCard.error = "標籤文字尚未輸入"
            } else {
                binding.catalogCard.error = null
            }
        }

        title.doOnTextChanged { text, start, count, after ->
            // action which will be invoked when the text is changing
            if (text.isNullOrEmpty()) {
                binding.titleCard.error = "標題文字尚未輸入"
            } else {
                binding.titleCard.error = null
            }
        }

        (tag.parent.parent as ViewGroup).setBackgroundColor(Color.parseColor("#EADDDB"))
        (title.parent.parent as ViewGroup).setBackgroundColor(Color.parseColor("#EADDDB"))

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
                val contentText = Snackbar.make(it,"不想填寫內容可以輸入空格唷~(･8･)",Snackbar.LENGTH_LONG)
                contentText.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
                contentText.setBackgroundTint(Color.parseColor("#EADDDB"))
                contentText.setTextColor(Color.parseColor("#181A19"))

                val snackBarView = contentText.view
                val params = snackBarView.layoutParams as FrameLayout.LayoutParams
                params.gravity =  Gravity.CENTER_HORIZONTAL and Gravity.TOP
                snackBarView.layoutParams = params

                contentText.show()
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

            //buidDrawingCache() only catch the first time data
            //only to destroy the origin data to render new view
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
                val contentText = Snackbar.make(it,"不想填寫內容可以輸入空格唷~(･8･)",Snackbar.LENGTH_LONG)
                contentText.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
                contentText.setBackgroundTint(Color.parseColor("#EADDDB"))
                contentText.setTextColor(Color.parseColor("#181A19"))

                val snackBarView = contentText.view
                val params = snackBarView.layoutParams as FrameLayout.LayoutParams
                params.gravity =  Gravity.CENTER_HORIZONTAL and Gravity.TOP
                params.setMargins(params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    params.bottomMargin + 100)
                snackBarView.layoutParams = params
                contentText.show()
            } else if (title.text.trim().isEmpty()) {
                //Snackbar ani
                val titleSnack = Snackbar.make(it,"資料未完成將填入預設值，免客氣",Snackbar.LENGTH_INDEFINITE)
                titleSnack.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
                titleSnack.setBackgroundTint(Color.parseColor("#EADDDB"))
                titleSnack.setTextColor(Color.parseColor("#181A19"))
                titleSnack.setAction("感恩的心") {
                        title.setText(UserManager.user.userName)
                    }
                    .setActionTextColor(Color.parseColor("#181A19"))
                val snackBarView = titleSnack.view
                val params = snackBarView.layoutParams as FrameLayout.LayoutParams
                params.gravity =  Gravity.CENTER_HORIZONTAL and Gravity.TOP
                params.setMargins(params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    params.bottomMargin + 100)
                snackBarView.layoutParams = params
                titleSnack.show()

            } else if (tag.text.trim().isEmpty()) {
                val tagSnack =
                    Snackbar.make( it,"資料未完成將填入預設值，免客氣", Snackbar.LENGTH_INDEFINITE)
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                    .setBackgroundTint(Color.parseColor("#EADDDB"))
                    .setTextColor(Color.parseColor("#181A19"))
                    .setAction("感謝有你") {
                        tag.setText("傻逼日常")
                    }.setActionTextColor(Color.parseColor("#181A19"))
                val snackBarView = tagSnack.view
                val params = snackBarView.layoutParams as FrameLayout.LayoutParams
                params.gravity =  Gravity.CENTER_HORIZONTAL and Gravity.TOP
                params.setMargins(params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    params.bottomMargin + 100)
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
                                title.text.toString(),
                                tag.text.toString(),
                                publishBitmap.width,
                                publishBitmap.height)
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
//        Log.i("screenWidth", "screenWidth = $screenWidth")
//        Log.i("screenHeight", "screenHeight = $screenHeight")

        //After combine Image Height
        //**require number type is float but the return number (after operating) is Int
        //螢幕寬 除以 圖片寬 乘以 圖片高 = 符合畫面比例高
        val height = (screenWidth.toFloat() / oriWidth.toFloat() * oriHeight.toFloat()).toInt()
        //our image will fit the screen width
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