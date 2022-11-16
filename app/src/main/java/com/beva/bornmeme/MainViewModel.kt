package com.beva.bornmeme

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class MainViewModel(userId: String): ViewModel() {

    val userData = MutableLiveData<List<User>>()

    init {
        getUser(userId)
    }

    private fun getUser (userId : String): MutableLiveData<List<User>> {
        if (userId == UserManager.user.userId) {
            Firebase.firestore.collection("Users")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    val list = mutableListOf<User>()
                    for (document in snapshot!!) {
                        val data = document.toObject(User::class.java)
                        list.add(data)
                    }
                    userData.value = list
                }
        }
        return userData
    }
}