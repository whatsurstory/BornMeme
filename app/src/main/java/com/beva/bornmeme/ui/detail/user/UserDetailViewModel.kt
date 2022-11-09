package com.beva.bornmeme.ui.detail.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.User
import com.beva.bornmeme.ui.editFragment.WHO
import com.bumptech.glide.Glide.init
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import timber.log.Timber

class UserDetailViewModel : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    init {
        getData()
    }

    //documentPath -> 帶參數 去看自己跟別人的頁面
    private fun getData() {
        val data = FirebaseFirestore.getInstance()
            .collection("Users")
            .document("cNXUG5FShzYesEOltXUZ")
        data.addSnapshotListener { snapshot, exception ->
            val user = snapshot?.toObject(User::class.java)
//            Timber.d("user $user")
            exception?.let {
                Timber.d("Exception ${it.message}")
            }
            _user.value = user
        }
    }
}