package com.beva.bornmeme.ui.detail.dialog

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.DialogCommentBinding
import com.beva.bornmeme.model.Comment
import com.beva.bornmeme.model.UserManager
import com.beva.bornmeme.model.UserManager.user
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class PublishViewModel : ViewModel() {

    fun publishComment(
        postId: String,
        parentId: String,
        binding: DialogCommentBinding,
        application: Application?
    ) {
        val fireStore = application?.let {
            FirebaseFirestore.getInstance()
                .collection(it.getString(R.string.comment_collection_text))
        }
        val document = fireStore?.document()
        val publish = Comment(
            commentId = document?.id,
            userId = UserManager.user.userId.toString(),
            postId = postId,
            time = Timestamp.now(),
            content = binding.editPublishContent.text.toString(),
            parentId = parentId
        )

        document?.set(publish)?.addOnSuccessListener {
            //                Timber.d("Publish Done")
            UserManager.user.userId?.let { id ->
                FirebaseFirestore.getInstance()
                    .collection(application.getString(R.string.user_collection_text))
                    .document(id)
                    .update("commentsId", FieldValue.arrayUnion(document.id))
            }
        }?.addOnFailureListener { e ->
            Timber.d("Error $e")
        }
    }

    @SuppressLint("SetTextI18n")
    fun getUserName(parentId: String, binding: DialogCommentBinding, application: Application?) {
        application?.getString(R.string.user_collection_text)?.let { user ->
            Firebase.firestore.collection(user)
                .whereArrayContains("commentsId", parentId)
                .get()
                .addOnCompleteListener {
                    for (item in it.result) {
                        //                    Timber.d("user name -> ${item.contains("userName")}")
                        binding.replyWhoText.text = "Reply to : ${item.data["userName"]}"
                    }
                }
        }
    }
}

