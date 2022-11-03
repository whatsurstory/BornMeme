package com.beva.bornmeme.model

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Resource(
    val type: String = "",
    val url: String? = null,
    val text: String? = null
) : Parcelable
