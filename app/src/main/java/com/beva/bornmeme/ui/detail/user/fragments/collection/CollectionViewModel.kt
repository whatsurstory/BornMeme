package com.beva.bornmeme.ui.detail.user.fragments.collection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Folder
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import timber.log.Timber

class CollectionViewModel : ViewModel() {

    val liveData = MutableLiveData<List<Folder>>()

    //Handle navigation to dialog
    private val _navigateToDetail = MutableLiveData<Folder>()

    val navigateToDetail: LiveData<Folder>
        get() = _navigateToDetail

    init {
        getData()
    }

    private fun getData(): MutableLiveData<List<Folder>> {
        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(UserManager.user.userId)
            .collection("Folders")
            .addSnapshotListener { snapshot, exception ->
                if (snapshot != null) {
                    Timber.d("item ${snapshot.documents}")
                }
            exception?.let {
                Timber.d("Exception ${it.message}")
            }
            val list = mutableListOf<Folder>()
            for (document in snapshot!!) {
                Timber.d("item ${document.id} ${document.data}")
                val item = document.toObject(Folder::class.java)
                Timber.d("item $item")
                list.add(item)
            }
            liveData.value = list
        }
        return liveData
    }

    fun navigateToDetail(item: Folder) {
        _navigateToDetail.value = item
    }

    fun onDetailNavigated() {
        _navigateToDetail.value = null
    }
}