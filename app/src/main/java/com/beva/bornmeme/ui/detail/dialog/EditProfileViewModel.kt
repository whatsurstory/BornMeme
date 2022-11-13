package com.beva.bornmeme.ui.detail.dialog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditProfileViewModel: ViewModel() {
        var name = MutableLiveData<String>()
        var email = MutableLiveData<String>()
        var desc = MutableLiveData<String>()
}