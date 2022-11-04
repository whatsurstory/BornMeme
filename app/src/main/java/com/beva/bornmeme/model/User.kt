package com.beva.bornmeme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val userId: String = "",
    val profilePhoto: String = "",
    val userName: String = "",
    val introduce: String = "",
    val LoginTimes: String = "",
    val RegisterTime: String = "",
    val commentsId: Int? = null,
    val collection: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val postQuantity: String = "",
    val blockList: List<String> = emptyList(),
    val followList: List<String> = emptyList()
) : Parcelable
