package com.beva.bornmeme.model

import android.graphics.Bitmap
import android.net.Uri

data class ImgResource(
    val type: String,
    val url: Bitmap,
    val text: String
)
