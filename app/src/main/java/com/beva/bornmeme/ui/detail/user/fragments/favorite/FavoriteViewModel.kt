package com.beva.bornmeme.ui.detail.user.fragments.favorite

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.R
import com.beva.bornmeme.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class FavoriteViewModel(userId: String, application: Application?) : ViewModel() {

    val likeData = MutableLiveData<List<Post>>()

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
                .whereArrayContains("like", userId)
                .addSnapshotListener { snapshot, e ->
                    val list = mutableListOf<Post>()
                    for (document in snapshot!!) {

                        val post = document.toObject(Post::class.java)
                        list.add(post)
                    }
                    likeData.value = list
                }
        }
        return likeData

    }

    fun navigateToDetail(item: Post) {
        _navigateToDetail.value = item
    }

    fun onDetailNavigated() {
        _navigateToDetail.value = null
    }
}