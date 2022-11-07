package com.beva.bornmeme.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val userId: String = "",
    val profilePhoto: String = "",
    val userName: String = "",
    val introduce: String = "",
    val LoginTimes: String = "",
    val RegisterTime: Timestamp? = null,
    val commentsId: List<String> = emptyList(),
    val collection: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val postQuantity: List<String> = emptyList(),
    val blockList: List<String> = emptyList(),
    val followList: List<String> = emptyList()
) : Parcelable
