package com.beva.bornmeme.model

import android.graphics.Bitmap
import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize


@Parcelize
data class Post(
    val id: String = "",
    val photoId: String = "",
    val ownerId: String = "",
    val title: String = "",
    val catalog: String = "",
    val url: String? = null,
    val like: List<String> = emptyList(),
    val resources: List<Resource>? = null,
    val collection: List<String> = emptyList(),
    val createdTime: Timestamp? = null,
) : Parcelable

//data class FeedPost(
//    val uid: String = "",
//    val username: String = "",
//    val image: String = "",
//    val caption: String = "",
//    val comments: List<Comment> = emptyList(),
//    val timestamp: Any = ServerValue.TIMESTAMP,
//    val photo: String? = null,
//    @get:Exclude
//    val id: String = "",
//    @get:Exclude
//    val commentsCount: Int = 0
//) {
//    fun timestampDate(): Date = Date(timestamp as Long)
//}

//data class SearchPost(
//    val image: String = "",
//    val caption: String = "",
//    val postId: String = "",
//    @get:Exclude
//    val id: String = "")