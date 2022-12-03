package com.beva.bornmeme.ui.detail.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class EditProfileViewModel(userId: String): ViewModel() {


        private val _user = MutableLiveData<User>()
        val user: LiveData<User>
                get() = _user

        var blockUser = emptyList<String>()
        var follower = emptyList<String>()
        var followUser = emptyList<String>()

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