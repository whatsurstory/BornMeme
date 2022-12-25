package com.beva.bornmeme.ui.edit_fragment

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.icu.util.Calendar
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.R
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.Date

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

    fun getImageUri(application: Application?, bitmap: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        val title = Calendar.getInstance().timeInMillis.toString()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val uriString = MediaStore.Images.Media.insertImage(
            application?.contentResolver,
            bitmap,
            title,
            null
        )
        return Uri.parse(uriString)
    }

    fun addNewPost(application: Application?,
                   uri: Uri?,
                   res: List<Any>,
                   title: String,
                   tag: String,
                   width: Int,
                   height: Int) {
//        Timber.d("getNewPost")
//        Timber.d("publish => $title tag $tag")
        val postPath = application?.let {
            FirebaseFirestore.getInstance()
                .collection(it.getString(R.string.post_collection_text)).document()
        }
        val userPath = application?.getString(R.string.user_collection_text)?.let {
            FirebaseFirestore.getInstance()
                .collection(it)
                .document(UserManager.user.userId.toString())
        }

        val ref = FirebaseStorage.getInstance().reference

        if (uri != null) {
            ref.child("img_edited/" + postPath?.id + ".jpg")
                .putFile(uri)
                .addOnSuccessListener {
                    it.metadata?.reference?.downloadUrl?.addOnSuccessListener { newUri ->
                        //這層的it才會帶到firebase return 的 Url
                        val post = hashMapOf(
                            "id" to postPath?.id,
                            "photoId" to "photo_id",
                            "ownerId" to UserManager.user.userId,
                            "title" to title,
                            "catalog" to tag,
                            "resources" to res,
                            "createdTime" to Date(Calendar.getInstance().timeInMillis),
                            "url" to newUri,
                            "imageWidth" to width,
                            "imageHeight" to height
                        )
                        //put into firebase_storage
                        postPath?.set(post)
                        userPath?.update("postQuantity", FieldValue.arrayUnion(postPath?.id))
//                        Timber.d("to firebase => Publish Done: POST ${postPath.id} \n USER ${userPath.id}")
                    }
                }
                .addOnFailureListener {
                    Timber.d("Error-> ${it.message}")
                }
        }
    }
}