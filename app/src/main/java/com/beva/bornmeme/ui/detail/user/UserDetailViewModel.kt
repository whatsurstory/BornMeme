package com.beva.bornmeme.ui.detail.user

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.R
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class UserDetailViewModel(userId: String, application: Application?) : ViewModel() {

    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User>
        get() = _userData

    init {
        getData(userId, application)
    }

    private fun getData(userId: String, application: Application?) {
        application?.let { app ->
            FirebaseFirestore.getInstance()
                .collection(app.getString(R.string.user_collection_text))
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

    fun add2follow(userId: String, application: Application?) {
        application?.let { app ->
            Firebase.firestore
                .collection(app.getString(R.string.user_collection_text))
                .document(UserManager.user.userId.toString())
                .update("followList", FieldValue.arrayUnion(userId))
                .addOnSuccessListener {
                    Firebase.firestore
                        .collection(app.getString(R.string.user_collection_text))
                        .document(userId)
                        .update("followers", FieldValue.arrayUnion(UserManager.user.userId))
                }
        }
    }

    fun cancel2Follow(userId: String, application: Application?) {
        application?.let { app ->
            Firebase.firestore
                .collection(app.getString(R.string.user_collection_text))
                .document(UserManager.user.userId.toString())
                .update("followList", FieldValue.arrayRemove(userId))
                .addOnSuccessListener {
                    Firebase.firestore
                        .collection(app.getString(R.string.user_collection_text))
                        .document(userId)
                        .update("followers", FieldValue.arrayRemove(UserManager.user.userId))
                }
        }
    }

}