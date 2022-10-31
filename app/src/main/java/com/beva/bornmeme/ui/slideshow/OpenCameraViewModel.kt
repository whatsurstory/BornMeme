package com.beva.bornmeme.ui.slideshow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OpenCameraViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Camera Fragment"
    }
    val text: LiveData<String> = _text
}