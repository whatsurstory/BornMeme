package com.beva.bornmeme.ui.detail.user.fragments.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class FavoriteViewModel(private val userId: String) : ViewModel() {

    val likeData = MutableLiveData<List<Post>>()

    private val _navigateToDetail = MutableLiveData<Post>()

    val navigateToDetail: LiveData<Post>
        get() = _navigateToDetail

    init {
        getData(userId)
    }

    private fun getData(userId: String): MutableLiveData<List<Post>> {

        FirebaseFirestore.getInstance().collection("Posts")
            .whereArrayContains("like", userId)
            .addSnapshotListener { snapshot, e ->
                val list = mutableListOf<Post>()
                for (document in snapshot!!) {
                    Timber.d("Favorite snapshot ID ->${document.id} list -> ${document.data}")
                    val post = document.toObject(Post::class.java)
                    list.add(post)
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