package com.beva.bornmeme.model

import com.google.firebase.Timestamp


//tag -> extension will change to a list!!!
data class Image(
    val imageId:String ="",
    val title:String = "",
    val url:String = "",
    val tag: List<String> = emptyList(),
    val userId: List<String> = emptyList()
)
