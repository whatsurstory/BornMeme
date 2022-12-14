package com.beva.bornmeme.ui.detail.user.fragments.comments

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.R
import com.beva.bornmeme.model.Comment
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.UserManager
import com.beva.bornmeme.model.UserManager.user
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class CommentsViewModel(userId: String,
                        application: Context) : ViewModel() {

    data class UiState(
        val getPostImg: (
            context: Context,
            postId: String,
            onPostObtained: ((Post) -> Unit)
        ) -> Unit,
        val deleteComment: (context: Context, commentId: String) -> Unit
    )

    val postData = MutableLiveData<List<Comment>>()

    init {
        getData(userId, application)
    }

    //Post All Photo in Fragment
    private fun getData(userId: String, application: Context): MutableLiveData<List<Comment>> {
            FirebaseFirestore.getInstance()
                .collection(application.getString(R.string.comment_collection_text))
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, e ->
                    val list = mutableListOf<Comment>()
                    if (snapshot != null) {
                        for (document in snapshot) {
                            val post = document.toObject(Comment::class.java)
                            list.add(post)
                        }
                    }
                    postData.value = list
                }
        return postData
    }

    val uiState = UiState(
        getPostImg = { context, postId, onPostObtained ->
            Firebase.firestore
                .collection(context.getString(R.string.post_collection_text))
                .document(postId)
                .get()
                .addOnCompleteListener {
                    val post = it.result.toObject(Post::class.java)
                    if (post != null) {
                        return@addOnCompleteListener onPostObtained(post)
                    }
                }
        },
        { context, deleteComment ->
            Firebase.firestore
                .collection(context.getString(R.string.comment_collection_text))
                .document(deleteComment)
                .delete()
                .addOnCompleteListener {
                    UserManager.user.userId?.let { id ->
                        Firebase.firestore
                            .collection(context.getString(R.string.user_collection_text))
                            .document(id)
                            .update("commentsId", FieldValue.arrayRemove(deleteComment))
                    }
                }
        }
    )

}