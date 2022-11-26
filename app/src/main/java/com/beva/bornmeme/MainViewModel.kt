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

class MainViewModel: ViewModel() {

    val _user = MutableLiveData<User>(null)
    val user: LiveData<User>
        get() = _user


    fun setUser(user: User) {
        _user.value = user
    }
}