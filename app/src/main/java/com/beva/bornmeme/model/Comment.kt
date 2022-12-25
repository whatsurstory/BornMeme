package com.beva.bornmeme.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.database.ServerValue
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Comment(
    val commentId: String? = "",
    val userId: String = "",
    val postId: String = "",
    var time: Timestamp? = null,
    val content: String = "",
    val like: List<String> = emptyList(),
    val dislike: List<String> = emptyList(),
    val photoUrl: String = "",
    val parentId:String =""
) : Parcelable

