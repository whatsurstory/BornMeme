package com.beva.bornmeme.ui.detail.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.User
import com.bumptech.glide.Glide.init
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import timber.log.Timber

class UserDetailViewModel : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    init {
        getData()
    }

    private fun getData() {
        val data = FirebaseFirestore.getInstance()
            .collection("Users")
            .document("cNXUG5FShzYesEOltXUZ")
        data.addSnapshotListener { snapshot, exception ->
            val user = snapshot?.toObject(User::class.java)
            Timber.d("user $user")

            exception?.let {
                Timber.d("Exception ${it.message}")
            }
            _user.value = user
//            val list = mutableListOf<User>()
//            for (document in snapshot!!) {
//                val post = document.toObject(User::class.java)
//                list.add(post)
//            }
//
//            userData.value = list
        }
    }
}