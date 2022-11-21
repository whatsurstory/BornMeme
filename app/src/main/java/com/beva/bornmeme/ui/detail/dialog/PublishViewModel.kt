package com.beva.bornmeme.ui.detail.dialog

import android.icu.util.Calendar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.databinding.DialogCommentBinding
import com.beva.bornmeme.model.Comment
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.internal.cache.DiskLruCache
import timber.log.Timber
import java.util.*

class PublishViewModel: ViewModel() {

    fun publishComment(postId: String,parentId:String, binding: DialogCommentBinding) {
        val fireStore = FirebaseFirestore.getInstance().collection("Comments")
        val document = fireStore.document()
        val publish = hashMapOf(
            "commentId" to document.id,
            "content" to binding.editPublishContent.text.toString(),
            "dislike" to null,
            "like" to null,
            "parentId" to parentId,
            "photoUrl" to "",
            "postId" to postId,
            "time" to Date(Calendar.getInstance().timeInMillis),
            "userId" to UserManager.user.userId
        )

        document.set(publish).addOnSuccessListener {
            Timber.d("Publish Done")
            FirebaseFirestore.getInstance()
                .collection("Posts").document(postId)
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        Timber.d("${snapshot.id} ${snapshot.data}")
                    }
                }
        }.addOnFailureListener {
            Timber.d("Error $it")
        }
    }

}
