package com.beva.bornmeme.ui.editFragment

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.icu.util.Calendar
import android.net.Uri
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.UserManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
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

    fun addNewPost(uri: Uri?, res: List<Any>, title: String, tag: String, width: Int, height: Int) {
        Timber.d("getNewPost")
        Timber.d("publish => $title tag $tag")
        val postPath = FirebaseFirestore.getInstance().collection("Posts").document()
        val userPath = FirebaseFirestore.getInstance().collection("Users")
            .document(UserManager.user.userId.toString())
        val ref = FirebaseStorage.getInstance().reference

        if (uri != null) {
            ref.child("img_edited/" + postPath.id + ".jpg")
                .putFile(uri)
                .addOnSuccessListener {
                    it.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                        //這層的it才會帶到firebase return 的 Url
                        val post = hashMapOf(
                            "id" to postPath.id,
                            "photoId" to "photo_id",
                            "ownerId" to UserManager.user.userId,
                            "title" to title,
                            "catalog" to tag,
                            "resources" to res,
                            "createdTime" to Date(Calendar.getInstance().timeInMillis),
                            "url" to it,
                            "imageWidth" to width,
                            "imageHeight" to height
                        )
                        //put into firebase_storage
                        postPath.set(post)
                        userPath.update("postQuantity", FieldValue.arrayUnion(postPath.id))
                        Timber.d("to firebase => Publish Done: POST ${postPath.id} \n USER ${userPath.id}")
                    }
                }
                .addOnFailureListener {
                    Timber.d("Error-> ${it.message}")
                }
        }
    }

    fun tagSnackbarShow(it: View, tagText: EditText) {
        val tagSnack =
            Snackbar.make(it, "資料未完成將填入預設值，免客氣",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                .setBackgroundTint(Color.parseColor("#EADDDB"))
                .setTextColor(Color.parseColor("#181A19"))
                .setAction("感謝有你") {
                    tagText.setText("傻逼日常")
                }.setActionTextColor(Color.parseColor("#181A19"))
        val snackBarView = tagSnack.view
        val params = snackBarView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL and Gravity.TOP
        snackBarView.layoutParams = params
        tagSnack.show()
    }

    fun titleSnackbarShow(it: View, titleText: EditText) {
        //Snackbar ani
        val titleSnack =
            Snackbar.make(it, "資料未完成將填入預設值，免客氣",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                .setBackgroundTint(Color.parseColor("#EADDDB"))
                .setTextColor(Color.parseColor("#181A19"))
                .setAction("感恩的心") {
                    titleText.setText(UserManager.user.userName)
                }
                .setActionTextColor(Color.parseColor("#181A19"))
        val snackBarView = titleSnack.view
        val params = snackBarView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL and Gravity.TOP
        snackBarView.layoutParams = params
        titleSnack.show()
    }

    fun showContentEmptySnackBar(it: View) {
        val contentText = Snackbar.make(
            it, "不想填寫內容可以輸入空格唷~(･8･)",
            Snackbar.LENGTH_LONG
        )
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
            .setBackgroundTint(Color.parseColor("#EADDDB"))
            .setTextColor(Color.parseColor("#181A19"))
        val snackBarView = contentText.view
        val params = snackBarView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL and Gravity.TOP
        snackBarView.layoutParams = params
        contentText.show()
    }

}