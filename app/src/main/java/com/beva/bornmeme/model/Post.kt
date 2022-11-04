package com.beva.bornmeme.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcelable
import android.renderscript.ScriptGroup
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import java.io.InputStream


@Parcelize
data class Post(
    val id: String = "",
    val photoId: String = "",
    val ownerId: String = "",
    val title: String = "",
    val catalog: String = "",
    val url: String? = null,
    val like: List<String>? = null,
    val resources: List<Resource> = emptyList(),
    val collection: List<String> = emptyList(),
    val createdTime: Timestamp? = null,
) : Parcelable
//{
//    val newUrl: Bitmap
//        get() = BitmapFactory.decodeFile(url)
//}
