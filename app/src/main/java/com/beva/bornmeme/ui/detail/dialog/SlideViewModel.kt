package com.beva.bornmeme.ui.detail.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Folder
import com.beva.bornmeme.model.FolderData
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class SlideViewModel(folder: Folder) : ViewModel() {

    private val _folderItem = MutableLiveData<List<FolderData>>()
    val folderItem: LiveData<List<FolderData>>
        get() = _folderItem

    private val _navigateToDetail = MutableLiveData<FolderData>()

    val navigateToDetail: LiveData<FolderData>
        get() = _navigateToDetail

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

                val detailImage = mutableListOf<FolderData>()
                for (item in folder.posts) {
                    detailImage.add(item)
                }
                _folderItem.value = detailImage
            }
    }

    fun navigateToDetail(item: FolderData) {
        _navigateToDetail.value = item
    }

    fun onDetailNavigated() {
        _navigateToDetail.value = null
    }
}