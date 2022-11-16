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

class UserDetailViewModel(userId: String) : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    init {
        getData(userId)
    }

    private fun getData(userId: String) {
        val data = FirebaseFirestore.getInstance()
            .collection("Users")
            .document(userId)
        data.addSnapshotListener { snapshot, exception ->
            val user = snapshot?.toObject(User::class.java)
            exception?.let {
                Timber.d("Exception ${it.message}")
            }
            _user.value = user
        }
    }

}