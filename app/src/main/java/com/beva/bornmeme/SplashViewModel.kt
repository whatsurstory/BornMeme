package com.beva.bornmeme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SplashViewModel: ViewModel() {

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