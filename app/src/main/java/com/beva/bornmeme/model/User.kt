package com.beva.bornmeme.model

data class User(
    val userId: String,
    val profilePhoto: String?,
    val userName: String,
    val introduce: String?,
    val LoginTimes: Int,
    val RegisterTime: String,
    val commentsId: Int? = null,
    val collection: PhotoInformation,
    val followers: Int,
    val postQuantity: Int,
    val blockList: User?,
    val followList: User?
)
