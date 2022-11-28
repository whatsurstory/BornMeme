package com.beva.bornmeme.ui.detail.user

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.bumptech.glide.Glide.init
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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

    fun add2follow (userId:String) {
        Firebase.firestore
            .collection("Users")
            .document(UserManager.user.userId.toString())
            .update("followList",FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                Firebase.firestore.collection("Users").document(userId)
                    .update("followers", FieldValue.arrayUnion(UserManager.user.userId))
            }
    }

}