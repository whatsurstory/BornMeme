package com.beva.bornmeme.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Folder (
    val createTime: Timestamp? = null,
    val name: String = "",
    val posts: List<FolderData> = emptyList()
   ) : Parcelable

@Parcelize
data class FolderData (
    val id: String ="",
    val url:String =""
    ) : Parcelable
