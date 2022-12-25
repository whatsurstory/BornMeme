package com.beva.bornmeme.ui.detail.user.fragments.posts

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.R
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class PostsViewModel(userId: String, application: Application?) : ViewModel() {

    val postData = MutableLiveData<List<Post>>()

    private val _navigateToDetail = MutableLiveData<Post>()

    val navigateToDetail: LiveData<Post>
        get() = _navigateToDetail


    init {
        getData(userId, application)
    }


    private fun getData(userId: String, application: Application?): MutableLiveData<List<Post>> {
        application?.let { app ->
            FirebaseFirestore.getInstance()
                .collection(app.getString(R.string.post_collection_text))
                .whereEqualTo("ownerId", userId)
                .addSnapshotListener { snapshot, e ->
                    val list = mutableListOf<Post>()
                    for (document in snapshot!!) {
    //                    Timber.d("Post snapshot ID ->${document.id} list -> ${document.data}")
                        val post = document.toObject(Post::class.java)
                        list.add(post)
                    }
                    postData.value = list
                }
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