package com.beva.bornmeme.ui.detail.user.fragments.collection

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.R
import com.beva.bornmeme.model.Folder
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class CollectionViewModel(userId: String,
                          application: Context) : ViewModel() {

    val folderData = MutableLiveData<List<Folder>>()

    //Handle navigation to dialog
    private val _navigateToDetail = MutableLiveData<Folder>()

    val navigateToDetail: LiveData<Folder>
        get() = _navigateToDetail

    init {
        getData(userId, application)
    }

    private fun getData(userId: String,
                        application: Context
    ): MutableLiveData<List<Folder>> {
            FirebaseFirestore.getInstance()
                .collection(application.getString(R.string.user_collection_text))
                .document(userId)
                .collection(application.getString(R.string.folder_collection_text))
                .addSnapshotListener { snapshot, exception ->
                    if (snapshot != null) {
                        Timber.d("item ${snapshot.documents}")
                    }
                    exception?.let { e ->
                        Timber.d("Exception ${e.message}")
                    }
                    val list = mutableListOf<Folder>()
                    if (snapshot != null) {
                        for (document in snapshot) {
                            val item = document.toObject(Folder::class.java)
                            list.add(item)
                        }
                    }
                    folderData.value = list
                }
        return folderData
    }

    fun deleteFile(item: Folder, context: Context) {
        UserManager.user.userId?.let { it ->
                FirebaseFirestore.getInstance()
                    .collection(context.getString(R.string.user_collection_text))
                    .document(it)
                    .collection(context.getString(R.string.folder_collection_text))
                    .document(item.name).delete()
            }
        }

    fun navigateToDetail(item: Folder) {
        _navigateToDetail.value = item
    }

    fun onDetailNavigated() {
        _navigateToDetail.value = null
    }
}