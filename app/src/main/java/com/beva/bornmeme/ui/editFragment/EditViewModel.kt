package com.beva.bornmeme.ui.editFragment

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.icu.util.Calendar
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MobileNavigationDirections
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class EditViewModel : ViewModel() {


    fun mergeBitmap(
        firstImage: Bitmap,
        secondImage: Bitmap,
        thirdImage: Bitmap
    ): Bitmap {
        val result = Bitmap.createBitmap(firstImage.width, firstImage.height, firstImage.config)
        val canvas = Canvas(result)
        canvas.drawBitmap(firstImage, 0f, 0f, null)
        val secondImageLeft = (firstImage.width - secondImage.width).toFloat() / 2
        canvas.drawBitmap(secondImage, secondImageLeft, 0f, null)
        val thirdImageLeft = (firstImage.width - thirdImage.width).toFloat() / 2
        val thirdImageTop = (firstImage.height - thirdImage.height).toFloat()
        canvas.drawBitmap(thirdImage, thirdImageLeft, thirdImageTop, null)

        //圖片中心點放置座標(resource, left, top,(may be null)): (背景寬 - 內容寬) / 2
        //**require parameter type is float
//        Log.d("secondImageLeft","secondImageLeft = $secondImageLeft")
//        Log.d("thirdImageLeft","thirdImageLeft = $thirdImageLeft")
//        Log.d("thirdImageTop","thirdImageTop = $thirdImageTop")

        

        return result
    }

    fun getImageUri(inContext: Application?, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        val title = Calendar.getInstance().timeInMillis.toString()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext?.contentResolver,
            inImage,
            title,
            null
        )
        return Uri.parse(path)
    }

    //Put the bitmap into local gallery
//    private fun merge(view: View) {
//        val contentValues = ContentValues()
//        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis())
//        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//        val uri =
//            activity?.contentResolver?.insert(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                contentValues
//            )
//        Log.d("uri","$uri")
//        Log.d("contentValues" , "$contentValues")
//        uri?.apply {
//            val ops = activity?.contentResolver?.openOutputStream(this)
//            mergeBitmap(
//                bgBitmap,
//                binding.upperText.drawingCache,
//                binding.bottomText.drawingCache
//            )?.compress(Bitmap.CompressFormat.JPEG, 100, ops)
//            ops?.close()
//        }
//    }

    fun getNewPost(uri: Uri, res: List<Any>) {
        val fireStore = FirebaseFirestore.getInstance().collection("Posts")
        val document = fireStore.document()
        val ref = FirebaseStorage.getInstance().reference

        ref.child("img_edited/" + document.id + ".jpg")
            .putFile(uri)
            .addOnSuccessListener {
                it.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                    //這層的it才會帶到firebase return 的 Uri
                    Log.d("edit =>", "downloadUrl = $it")
                    Log.d("edit =>", "edit uri = $it => take it to Posts:url")

                    val post = hashMapOf(
                        "id" to document.id,
                        "photoId" to "photo_id",
                        "ownerId" to "Beva9487",
                        "title" to "test title",
                        "catalog" to "test tag",
                        "like" to null,
                        "resources" to res,
                        "collection" to null,
                        "createdTime" to Date(Calendar.getInstance().timeInMillis),
                        "url" to it
                    )
                    //put into firebase_storage
                    document.set(post)
                    Log.i("tofirebase =>", "Publish Done: $post")
                    //  Log.d("test","test uri = ${Uri.parse(uri.toString())}")
                }
            }
            .addOnFailureListener {
                Log.d("Error", "${it.message}")
            }
    }
}