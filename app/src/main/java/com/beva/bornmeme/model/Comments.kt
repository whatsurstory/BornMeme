package com.beva.bornmeme.model

import android.os.Parcelable
import com.google.firebase.database.ServerValue
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Comments(
    val commentId: Int,
    val userId: String,
    val username: String = "",
    val photoId: String,
    var createdTime: Long? = null,
    val content: String,
    val like: User,
    val dislike: User,
    val photoUrl: String = ""
) : Parcelable

//data class Comment(
//    val uid: String = "",
//    val username: String = "",
//    val photo: String? = null,
//    val text: String = "",
//    val timestamp: Any = ServerValue.TIMESTAMP,
//    @get:Exclude
//    val id: String = ""
//) {
//    fun timestampDate() = Date(timestamp as Long)
//}
