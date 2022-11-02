package com.beva.bornmeme.model

data class CommentsInformation(
    val commentId: Int,
    val userId: String,
    val photoId: String,
    val time: String,
    val content: String,
    val like: List<User>,
    val dislike: List<User>,
    val photoUrl: String? = ""
)
