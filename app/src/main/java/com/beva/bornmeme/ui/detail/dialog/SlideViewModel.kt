package com.beva.bornmeme.ui.detail.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Folder
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class SlideViewModel(folder: Folder) : ViewModel() {

    private val _imageItem = MutableLiveData<List<String>>()
    val imageItem: LiveData<List<String>>
        get() = _imageItem

    init {
        getImage(folder)
    }

    private fun getImage(folder: Folder) {
        FirebaseFirestore.getInstance()
            .collection("Users")
            .document(UserManager.user.userId.toString())
            .collection("Folders")
            .document(folder.name)
            .addSnapshotListener { snapshot, exception ->
                if (snapshot != null) {
                    Timber.d("item ${snapshot.id} ${snapshot.data}")
                }
                exception?.let {
                    Timber.d("Exception ${it.message}")
                }

                val detailImage = mutableListOf<String>()
                for (item in folder.posts) {
                    detailImage.add(item.url)
                }
                _imageItem.value = detailImage
            }
    }
}