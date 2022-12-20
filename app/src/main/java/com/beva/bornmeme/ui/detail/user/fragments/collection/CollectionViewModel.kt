package com.beva.bornmeme.ui.detail.user.fragments.collection

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.R
import com.beva.bornmeme.model.Folder
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class CollectionViewModel(userId: String, context: Context) : ViewModel() {

    val folderData = MutableLiveData<List<Folder>>()

    //Handle navigation to dialog
    private val _navigateToDetail = MutableLiveData<Folder>()

    val navigateToDetail: LiveData<Folder>
        get() = _navigateToDetail

    init {
        getData(userId, context)
    }

    private fun getData(userId: String, context: Context): MutableLiveData<List<Folder>> {
        FirebaseFirestore.getInstance()
            .collection(context.getString(R.string.user_collection_text))
            .document(userId)
            .collection(context.getString(R.string.folder_collection_text))
            .addSnapshotListener { snapshot, exception ->
                if (snapshot != null) {
                    Timber.d("item ${snapshot.documents}")
                }
                exception?.let {
                    Timber.d("Exception ${it.message}")
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

    fun deleteFile(item: Folder,context: Context) {
        UserManager.user.userId?.let {
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