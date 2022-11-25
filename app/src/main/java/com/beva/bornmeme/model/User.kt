package com.beva.bornmeme.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var userId: String? = "",
    val profilePhoto: String = "",
    val userName: String = "",
    val introduce: String = "",
    val loginTimes: String = "",
    val email: String = "",
    val registerTime: Timestamp? = null,
    val commentsId: List<String> = emptyList(),
    val likeId: List<String> = emptyList(),
    val collection: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val postQuantity: List<String> = emptyList(),
    val blockList: List<String> = emptyList(),
    val followList: List<String> = emptyList()
) : Parcelable

