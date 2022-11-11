package com.beva.bornmeme.ui.detail.user.fragments.comments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Comment
import com.beva.bornmeme.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class CommentsViewModel : ViewModel() {

    val postData = MutableLiveData<List<Comment>>()

    init {
        getData()
    }

    //Post All Photo in Fragment
    private fun getData(): MutableLiveData<List<Comment>> {
        val collection = FirebaseFirestore.getInstance()
            .collection("Comments")

        collection.whereEqualTo("userId", "cNXUG5FShzYesEOltXUZ")
            .addSnapshotListener { snapshot, e ->
                val list = mutableListOf<Comment>()
                for (document in snapshot!!){
                    Timber.d("comment snapshot ID ->${document.id} list -> ${document.data}")
                    val post = document.toObject(Comment::class.java)
                    list.add(post)
                }
                postData.value = list
            }
        return postData
    }
}