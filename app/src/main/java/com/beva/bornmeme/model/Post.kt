package com.beva.bornmeme.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcelable
import android.renderscript.ScriptGroup
import com.google.firebase.Timestamp
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.InputStream


@Parcelize
data class Post(
    val id: String = "",
    val photoId: String = "",
    val ownerId: String = "",
    val title: String = "",
    val catalog: String = "",
    val url: String? = "",
    val like: List<String>? = emptyList(),
    val resources: List<Resource> = emptyList(),
    val collection: List<String>? =emptyList(),
    val createdTime: Timestamp? = null,
    val imageWidth: Int = -1,
    val imageHeight: Int = -1
) : Parcelable

