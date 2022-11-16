package com.beva.bornmeme.ui.editFragment

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.icu.util.Calendar
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber
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

    fun addNewPost(uri: Uri, res: List<Any>, title:String, tag:String) {
        Timber.d("getNewPost")

        val postPath = FirebaseFirestore.getInstance().collection("Posts").document()
        val userPath = FirebaseFirestore.getInstance().collection("Users").document()
        val ref = FirebaseStorage.getInstance().reference

        ref.child("img_edited/" + postPath.id + ".jpg")
            .putFile(uri)
            .addOnSuccessListener {
                it.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                    //這層的it才會帶到firebase return 的 Uri
                    Timber.d("edited uri: $it => take it to upload url")
                    Timber.d("newTag $tag")
                    val post = hashMapOf(
                        "id" to postPath.id,
                        "photoId" to "photo_id",
                        "ownerId" to UserManager.user.userId,
                        "title" to title,
                        "catalog" to tag,
                        "like" to null,
                        "resources" to res,
                        "collection" to null,
                        "createdTime" to Date(Calendar.getInstance().timeInMillis),
                        "url" to it
                    )
                    //put into firebase_storage
                    postPath.set(post)
                    userPath.update("postQuantity", FieldValue.arrayUnion(postPath.id))
                    Timber.d("to firebase => Publish Done: POST ${postPath.id} \n USER ${userPath.id}")
                    //  Log.d("test","test uri = ${Uri.parse(uri.toString())}")
                }
            }
            .addOnFailureListener {
                Timber.d("Error-> ${it.message}")
            }
    }
}