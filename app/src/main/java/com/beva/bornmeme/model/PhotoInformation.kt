package com.beva.bornmeme.model



data class PhotoInformation(
    val photoId: Int,
    val user: User?,
    val postTitle: String?,
    val catalog: String? = "",
    val url: String?,
    val like: User?,
    val resources: ImgResource,
    val collection: User?
)