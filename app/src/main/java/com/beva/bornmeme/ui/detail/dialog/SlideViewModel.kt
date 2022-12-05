package com.beva.bornmeme.ui.detail.dialog

import android.graphics.Bitmap
import android.os.Environment
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.model.Folder
import com.beva.bornmeme.model.FolderData
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class SlideViewModel(folder: Folder) : ViewModel() {

    private val _imageItem = MutableLiveData<List<FolderData>>()
    val imageItem: LiveData<List<FolderData>>
        get() = _imageItem

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
                _imageItem.value = detailImage
            }
    }

    // add okhttp instance to your view model or you inject it with hilt if your using dependency injection
//    private val okHttpClient = OkHttpClient.Builder().build()

    // add this function to your view model
//    fun downloadImage(imageUrl: String) {
//        val request = Request.Builder()
//            .url(imageUrl)
//            .build()
//
//        okHttpClient.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                // Download Failed, you can show error to the user
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                if (!response.isSuccessful) {
//                    // Download Failed, you can show error to the user
//                    return
//                }
//
//                response.body?.let { responseBody ->
//                    try {
//                        // Convert response body to byte array
//                        val imageByteArray = responseBody.byteStream().readBytes()
//
//                        // Split image url so we can get the image name
//                        val words = imageUrl.split("/").toTypedArray()
//
//                        // Get the image name
//                        val imageName = words.last()
//
//                        // Init pathName (Downloads Directory)
//                        val pathName = "${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_DOWNLOADS}"
//
//                        // Create New file for the image
//                        val file = File(pathName, imageName)
//
//                        // Set byteArray To Image File
//                        file.writeBytes(imageByteArray)
//                    } catch(e: IOException) {
//                        // Saving Image Failed, you can show error to the user
//                        e.printStackTrace()
//                    }
//                }
//            }
//        })
//    }

    fun navigateToDetail(item: FolderData) {
        _navigateToDetail.value = item
    }

    fun onDetailNavigated() {
        _navigateToDetail.value = null
    }


}