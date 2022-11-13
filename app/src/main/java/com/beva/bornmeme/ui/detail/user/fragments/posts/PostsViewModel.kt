package com.beva.bornmeme.ui.detail.user.fragments.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class PostsViewModel : ViewModel() {

    val postData = MutableLiveData<List<Post>>()

    private val _navigateToDetail = MutableLiveData<Post>()

    val navigateToDetail: LiveData<Post>
        get() = _navigateToDetail

    init {
        getData()
    }


    private fun getData(): MutableLiveData<List<Post>> {
        val collection =FirebaseFirestore.getInstance().collection("Posts")

        collection.whereEqualTo("ownerId", "cNXUG5FShzYesEOltXUZ")
            .addSnapshotListener { snapshot, e ->
                val list = mutableListOf<Post>()
                for (document in snapshot!!){
                    Timber.d("Post snapshot ID ->${document.id} list -> ${document.data}")
                    val post = document.toObject(Post::class.java)
                    list.add(post)
                }
                postData.value = list
            }
         return postData
    }

    fun navigateToDetail(item: Post) {
        _navigateToDetail.value = item
    }

    fun onDetailNavigated() {
        _navigateToDetail.value = null
    }
}