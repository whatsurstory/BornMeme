package com.beva.bornmeme.ui.detail.dialog

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.R
import com.beva.bornmeme.model.Folder
import com.beva.bornmeme.model.FolderData
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class SlideViewModel(folder: Folder, application: Application?) : ViewModel() {

    private val _folderItem = MutableLiveData<List<FolderData>>()
    val folderItem: LiveData<List<FolderData>>
        get() = _folderItem

    private val _navigateToDetail = MutableLiveData<FolderData>()

    val navigateToDetail: LiveData<FolderData>
        get() = _navigateToDetail

    init {
        getImage(folder, application)
    }

    private fun getImage(folder: Folder, application: Application?) {
        application?.let {
            FirebaseFirestore.getInstance()
                .collection(it.getString(R.string.user_collection_text))
                .document(UserManager.user.userId.toString())
                .collection(it.getString(R.string.folder_collection_text))
                .document(folder.name)
                .addSnapshotListener { snapshot, exception ->
                    if (snapshot != null) {
                        Timber.d("item ${snapshot.id} ${snapshot.data}")
                    }
                    exception?.let { e ->
                        Timber.d("Exception ${e.message}")
                    }

                    val detailImage = mutableListOf<FolderData>()
                    for (item in folder.posts) {
                        detailImage.add(item)
                    }
                    _folderItem.value = detailImage
                }
        }
    }

    fun navigateToDetail(item: FolderData) {
        _navigateToDetail.value = item
    }

    fun onDetailNavigated() {
        _navigateToDetail.value = null
    }
}