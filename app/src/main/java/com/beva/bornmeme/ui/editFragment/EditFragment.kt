package com.beva.bornmeme.ui.editFragment


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.collection.arrayMapOf
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.databinding.FragmentEditFixmodeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber


class EditFragment : Fragment() {
//TODO: REPOSITORY to be complete

    private lateinit var binding: FragmentEditFixmodeBinding
    private lateinit var uri: Uri
    private lateinit var upperText: EditText
    private lateinit var bottomText: EditText
    //complete the publish will input the photo to firebase, Using  Path -> Posts
    private val fireStore = FirebaseFirestore.getInstance().collection("Posts")
    private val document = fireStore.document()

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
        val viewModel = ViewModelProvider(this).get(EditViewModel::class.java)
        upperText = binding.upperText
        bottomText = binding.bottomText


        //to preview
        binding.previewBtn.setOnClickListener {
            Timber.d("onClick Preview")

            if (upperText.text.isNullOrEmpty() || bottomText.text.isNullOrEmpty()){

                Snackbar.make(it, "Not Adding Text Yet", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
            } else {

                val baseBitmap = getBitmapByUri(uri)
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
            if (upperText.text.isNullOrEmpty() || bottomText.text.isNullOrEmpty()) {
            Snackbar.make(it, "Not Adding Text Yet", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()

            } else {

                val ref = FirebaseStorage.getInstance().reference
                ref.child("img_origin/" + document.id + ".jpg")
                    .putFile(uri)
                    .addOnSuccessListener {
                        it.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                            //這層的it才會帶到firebase return 的 Uri
                            Timber.d("origin uri: $it => take it to base url")

                            val res = listOf(
                                arrayMapOf("type" to "base", "url" to it),
                                (arrayMapOf(
                                    "type" to "text",
                                    "url" to upperText.text.toString() + bottomText.text.toString()
                                ))
                            )

                            val baseBitmap = getBitmapByUri(uri)
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
                                Timber.d("newUri => $newUri")
                            if (newUri != null) {
                                viewModel.addNewPost(newUri, res)
                            }
                            findNavController().navigate(MobileNavigationDirections.navigateToHomeFragment())

                        }?.addOnFailureListener {
                            Timber.d("upload uri Error => $it")
                        }

                    }.addOnFailureListener {
                        Timber.d("TaskSnapshot Error => $it")
                    }
            }
        }
        return binding.root
    }

    private fun getBitmapByUri(bitmapUri: Uri): Bitmap {
        return BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(bitmapUri))
    }

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