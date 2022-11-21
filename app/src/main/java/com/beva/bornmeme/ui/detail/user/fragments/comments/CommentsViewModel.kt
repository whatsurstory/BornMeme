package com.beva.bornmeme.ui.detail.user.fragments.comments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Comment
import com.beva.bornmeme.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class CommentsViewModel(userId: String) : ViewModel() {

    val postData = MutableLiveData<List<Comment>>()

    init {
        getData(userId)
    }

    //Post All Photo in Fragment
    private fun getData(userId: String): MutableLiveData<List<Comment>> {
        Timber.d("user $userId")
        val collection = FirebaseFirestore.getInstance()
            .collection("Comments")

        collection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                Timber.d("comment e ->${e?.message}")
                val list = mutableListOf<Comment>()
                for (document in snapshot!!){
                    Timber.d("comment snapshot ID ->${document.id} " +
                            "list -> ${document.data}")
                    val post = document.toObject(Comment::class.java)
                    list.add(post)
                }
                postData.value = list
            }
        return postData
    }
    fun getPostImage (postId: String) {
        Firebase.firestore
            .collection("Posts")
            .document(postId)
            .get()
            .addOnCompleteListener { task ->
                Timber.d("task -> ${task.result.data}")
            }
    }
}