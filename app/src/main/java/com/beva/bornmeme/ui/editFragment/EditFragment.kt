package com.beva.bornmeme.ui.editFragment


import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.collection.arrayMapOf
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.databinding.FragmentEditFixmodeBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*


class EditFragment : Fragment() {

    private lateinit var binding: FragmentEditFixmodeBinding
    private lateinit var uri: Uri
    private lateinit var bgBitmap: Bitmap

    //complete the publish will input the photo to firebase, Using  Path -> Posts
    private val fireStore = FirebaseFirestore.getInstance().collection("Posts")
    private val document = fireStore.document()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentEditFixmodeBinding.inflate(layoutInflater)

        arguments?.let { bundle ->
            uri = bundle.getParcelable("uri")!!
            Log.d("From Gallery", "get uri= $uri")
        }
        layoutPrams()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this).get(EditViewModel::class.java)
        val photo = binding.originPhoto
        val upperText = binding.upperText
        val bottomText = binding.bottomText
        //toString put out side can make empty success
        upperText.toString()
        bottomText.toString()


        //to preview
        binding.previewBtn.setOnClickListener {
            transfer2Bitmap(upperText, bottomText, photo)
            //For Preview, so we put arguments to show the image
            binding.originPhoto.setImageBitmap(
                viewModel.mergeBitmap(
                    bgBitmap,
                    binding.upperText.drawingCache,
                    binding.bottomText.drawingCache,
                    true,
                    this
                )
            )
        }

        //to publish
        binding.publishBtn.setOnClickListener {
            val ref = FirebaseStorage.getInstance().reference

            ref.child("img_origin/" + document.id + ".jpg")
                .putFile(uri)
                .addOnSuccessListener {
                    it.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                        //這層的it才會帶到firebase return 的 Uri
                        Log.d("origin =>", "downloadUrl = $it")
                        Log.d("origin =>", "origin uri = $uri")
                        transfer2Bitmap(upperText, bottomText, photo)
                        val postData = viewModel.mergeBitmap(
                            bgBitmap,
                            binding.upperText.drawingCache,
                            binding.bottomText.drawingCache,
                            false, this
                        )

                        val newUri = viewModel.getImageUri(activity?.application, postData)
                        //the data need uploading to firebase which will storage and share on different devise
                        //val res = listOf(arrayMapOf("type" to "base", "url" to uri),(arrayMapOf("type" to "text", "url" to upperText.toString() + bottomText.toString())))
                        //For Saving Post
                        val post = hashMapOf(
                            "id" to document.id,
                            "photoId" to "photo_id",
                            "ownerId" to "Beva9487",
                            "title" to "test title",
                            "catalog" to "test tag",
                            "like" to null,
                            "resources" to null,
                            "collection" to null,
                            "createdTime" to Date(Calendar.getInstance().timeInMillis),
                            "url" to newUri
                        )

                        //put into firebase_storage
                        document.set(post)
                        Log.i("tofirebase =>", "Publish Done: $post")
                        //  Log.d("test","test uri = ${Uri.parse(uri.toString())}")
                        if (newUri != null) {
                            viewModel.getNewPost(newUri)
                        }
                        findNavController().navigate(MobileNavigationDirections.navigateToHomeFragment())
                    }
                }.addOnFailureListener {
                    Log.d("Error", "${it.message}")
                }
        }
        return binding.root
    }


    private fun transfer2Bitmap(upperText: EditText, bottomText: EditText, photo: ImageView) {
        upperText.buildDrawingCache()
        bottomText.buildDrawingCache()
        bgBitmap = BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(uri))
        photo.setImageBitmap(bgBitmap)
        photo.setImageBitmap(binding.upperText.drawingCache)
        photo.setImageBitmap(binding.bottomText.drawingCache)
    }

    private fun layoutPrams() {
        //TODO: Fixed the Text Size not same
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
        Log.w("oriWidth", "oriWidth=${oriWidth}")
        Log.w("oriHeight", "oriHeight=${oriHeight}")

        //Screen ViewWidth & Height
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
//        Log.i("screenWidth", "screenWidth = $screenWidth")
//        Log.i("screenHeight", "screenHeight = $screenHeight")

        //After combine Image Height
        //**require number type is float but the return number (after operating) is Int
        //螢幕寬 除以 圖片寬 乘以 圖片高 = 符合畫面比例高
        val height = (screenWidth.toFloat() / oriWidth.toFloat() * oriHeight.toFloat()).toInt()
        //our image will fit the screen width
        Log.d("final width", "width = $screenWidth")
        Log.d("final height", "height = $height")

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