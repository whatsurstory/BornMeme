package com.beva.bornmeme.ui.detail.dialog

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.R
import com.beva.bornmeme.model.User
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class EditProfileViewModel(userId: String, context: Context) : ViewModel() {


    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User>
        get() = _userData

    var blockUser = emptyList<String>()
    var follower = emptyList<String>()
    var followUser = emptyList<String>()

    init {
        getData(userId, context)
    }

    private fun getData(userId: String, context: Context) {
        FirebaseFirestore.getInstance()
            .collection(context.getString(R.string.user_collection_text))
            .document(userId)
            .addSnapshotListener { snapshot, exception ->
                val user = snapshot?.toObject(User::class.java)
                exception?.let {
                    Timber.d("Exception ${it.message}")
                }
                _userData.value = user
            }
    }

}