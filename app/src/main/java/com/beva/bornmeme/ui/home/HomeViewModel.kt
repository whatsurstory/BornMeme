package com.beva.bornmeme.ui.home

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.Resource
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel : ViewModel() {

     val liveData = MutableLiveData<List<Post>>()

    // Handle navigation to detail
    private val _navigateToDetail = MutableLiveData<Post>()

    val navigateToDetail: LiveData<Post>
        get() = _navigateToDetail

    init {
        getData()
    }


    //Post All Photo in Fragment
    private fun getData(): MutableLiveData<List<Post>> {
        val postData = FirebaseFirestore.getInstance()
            .collection("Posts")
        postData.addSnapshotListener { snapshot, exception ->
            Log.i("Bevaaaaa", "addSnapshotListener detect")

            exception?.let {
                Log.w("Bevaaaaa", "[${this::class.simpleName}] Error getting documents. ${it.message}")
            }

            val list = mutableListOf<Post>()
            for (document in snapshot!!) {
                val post = document.toObject(Post::class.java)
                list.add(post)
            }

            liveData.value = list
        }
        return liveData
    }

    fun navigateToDetail(item: Post) {
        _navigateToDetail.value = item
    }

    fun onDetailNavigated() {
        _navigateToDetail.value = null
    }

    }
