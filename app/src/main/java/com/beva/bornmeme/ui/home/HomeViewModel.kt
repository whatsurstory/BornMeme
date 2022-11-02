package com.beva.bornmeme.ui.home

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.ImgResource
import com.beva.bornmeme.model.PhotoInformation
import com.google.android.gms.common.data.DataBufferUtils.hasNextPage
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _list = MutableLiveData<List<ImgResource>>()
    val list: LiveData<List<ImgResource>>
        get() = _list

    fun getPostResult(item: Bitmap) {
        val list = mutableListOf<ImgResource>()
        for (i in 0..10) {
            list.add(ImgResource("",item,""))
            }
        }
    }
