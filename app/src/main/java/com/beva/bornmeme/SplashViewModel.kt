package com.beva.bornmeme

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class SplashViewModel : ViewModel() {

    // Handle leave
    private val _leaveSplash = MutableLiveData<Boolean>()

    val leaveSplash: LiveData<Boolean>
        get() = _leaveSplash

    fun leave() {
        _leaveSplash.value = true
    }

    fun onLeaveCompleted() {
        _leaveSplash.value = null
    }

}