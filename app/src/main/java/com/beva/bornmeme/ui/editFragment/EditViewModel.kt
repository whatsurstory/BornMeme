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
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.*
enum class WHO(val value: String) {
    BEVA("cNXUG5FShzYesEOltXUZ"),
    MINDY("uPpAnxXzhoj37e6uIG84")
}
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

    fun addNewPost(uri: Uri, res: List<Any>) {
        Timber.d("getNewPost")

        val document = FirebaseFirestore.getInstance().collection("Posts").document()
        val ownerId = FirebaseFirestore.getInstance()
            .collection("Users").document("cNXUG5FShzYesEOltXUZ")
        val ref = FirebaseStorage.getInstance().reference

        ref.child("img_edited/" + document.id + ".jpg")
            .putFile(uri)
            .addOnSuccessListener {
                it.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                    //這層的it才會帶到firebase return 的 Uri
                    Timber.d("edited uri: $it => take it to upload url")

                    val post = hashMapOf(
                        "id" to document.id,
                        "photoId" to "photo_id",
                        "ownerId" to ownerId,
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
                    Timber.d("to firebase => Publish Done: $post")
                    //  Log.d("test","test uri = ${Uri.parse(uri.toString())}")
                }
            }
            .addOnFailureListener {
                Timber.d("Error-> ${it.message}")
            }
    }
}