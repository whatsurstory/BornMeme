package com.beva.bornmeme.ui.gallery

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Image
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class GalleryViewModel : ViewModel() {

    val imageData = MutableLiveData<List<Image>>()

    init {
        getData()
    }

    private fun getData(): MutableLiveData<List<Image>> {
        FirebaseFirestore.getInstance()
            .collection("Modules")
            .addSnapshotListener { snapshot, exception ->

                exception?.let {
                    Timber.d("Exception ${it.message}")
                }

                val image = mutableListOf<Image>()
                for (document in snapshot!!) {
                    val img = document.toObject(Image::class.java)
                    image.add(img)
                }
                imageData.value = image
            }
        return imageData
    }
}