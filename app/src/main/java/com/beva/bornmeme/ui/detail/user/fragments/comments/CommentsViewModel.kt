package com.beva.bornmeme.ui.detail.user.fragments.comments

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.R
import com.beva.bornmeme.model.Comment
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.UserManager.user
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class CommentsViewModel(userId: String, context: Context) : ViewModel() {

    data class UiState(
        val getPostImg: (
            postId: String,
            onPostObtained: ((Post) -> Unit)
        ) -> Unit
    )

    val postData = MutableLiveData<List<Comment>>()

    init {
        getData(userId,context)
    }

    //Post All Photo in Fragment
    private fun getData(userId: String, context:Context): MutableLiveData<List<Comment>> {
        FirebaseFirestore.getInstance()
            .collection(context.getString(R.string.comment_collection_text))
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
        getPostImg = { postId, onPostObtained ->
            Firebase.firestore
                .collection("Posts")
                .document(postId)
                .get()
                .addOnCompleteListener {
                    val post = it.result.toObject(Post::class.java)
                    if (post != null) {
                        return@addOnCompleteListener onPostObtained(post)
                    }
                }
            }
        )

}