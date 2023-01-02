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

class FavoriteViewModel(userId: String, application: Context) : ViewModel() {

    val likeData = MutableLiveData<List<Post>>()

    private val _navigateToDetail = MutableLiveData<Post>()

    val navigateToDetail: LiveData<Post>
        get() = _navigateToDetail

    init {
        getData(userId, application)
    }

    private fun getData(userId: String, application: Context): MutableLiveData<List<Post>> {

            FirebaseFirestore.getInstance()
                .collection(application.getString(R.string.post_collection_text))
                .whereArrayContains("like", userId)
                .addSnapshotListener { snapshot, e ->

                    e?.let {
                        Timber.d("Exception ${it.message}")
                    }

                    val list = mutableListOf<Post>()
                    if (snapshot != null) {
                        for (document in snapshot) {
                            val post = document.toObject(Post::class.java)
                            list.add(post)
                        }
                    }
                    likeData.value = list
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