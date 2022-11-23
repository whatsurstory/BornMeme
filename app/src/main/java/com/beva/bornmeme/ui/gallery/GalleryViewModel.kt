package com.beva.bornmeme.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Folder
import com.beva.bornmeme.model.Image
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import timber.log.Timber

class GalleryViewModel : ViewModel() {

    val liveData = MutableLiveData<List<Image>>()

    init {
        getData()
    }

    private fun getData(): MutableLiveData<List<Image>> {
        FirebaseFirestore.getInstance()
            .collection("Modules")
            .addSnapshotListener { snapshot, exception ->
                val detailImage = mutableListOf<Image>()
                for (document in snapshot!!) {
                    val img = document.toObject(Image::class.java)
                    detailImage.add(img)
                }
                liveData.value = detailImage
            }
        return  liveData
    }

}