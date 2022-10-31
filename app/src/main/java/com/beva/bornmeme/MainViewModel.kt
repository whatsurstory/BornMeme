package com.beva.bornmeme

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    private val _nav2Camera = MutableLiveData<Boolean?>()

    val nav2Camera: LiveData<Boolean?>
        get() = _nav2Camera

    fun navigate2Camera() {
        Log.d("navigate value=","${_nav2Camera.value}")
        _nav2Camera.value = true
        Log.d("navigate value=","${_nav2Camera.value}")
        _nav2Camera.value = _nav2Camera.value
    }

    fun onCameraNavigated() {
        _nav2Camera.value = null
    }
}