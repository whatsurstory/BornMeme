package com.beva.bornmeme.ui.detail.user.fragments.posts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class PostsViewModel : ViewModel() {

    val postData = MutableLiveData<List<Post>>()

    init {
        getData()
    }

    //Post All Photo in Fragment
    private fun getData(): MutableLiveData<List<Post>> {
        val collection =FirebaseFirestore.getInstance().collection("Posts")

        collection.whereEqualTo("ownerId", "beva9487")
            .addSnapshotListener { snapshot, e ->
                val list = mutableListOf<Post>()
                for (document in snapshot!!){
                    Timber.d("snapshot ID ->${document.id} list -> ${document.data}")
                    val post = document.toObject(Post::class.java)
                    list.add(post)
                }
                postData.value = list
            }
//        collection.whereEqualTo("ownerId", "beva9487")
//            .get()
//            .addOnSuccessListener {
//                for (id in it){
//
//                }
//            }
//            .addOnFailureListener {
//                Timber.d("Error $it")
//            }

        return postData
    }
}