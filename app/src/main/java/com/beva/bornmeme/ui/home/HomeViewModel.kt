package com.beva.bornmeme.ui.home

import android.graphics.Bitmap
import android.os.Build.VERSION_CODES.M
import android.util.Log
import androidx.lifecycle.*
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.Resource
import com.beva.bornmeme.ui.detail.img.CommentCell
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import timber.log.Timber

class HomeViewModel : ViewModel() {

     val liveData = MutableLiveData<List<Post>>()

    private var _tagSet = MutableLiveData<String>()
    private val tagSet: MutableLiveData<String>
        get() = _tagSet

    // Handle navigation to detail
    private val _navigateToDetail = MutableLiveData<Post>()

    val navigateToDetail: LiveData<Post>
        get() = _navigateToDetail



    init {
        getData()
    }

        val tagCell = Transformations.map(liveData) {
            val cells = mutableListOf<String>()
            for (item in it) {
                val tag = item.catalog
                //包含了相同的字串就不加入list
                if (!cells.contains(tag)) {
                    cells.add(tag)
                }
//                Timber.d("Post cells $cells")
            }
            cells
        }


    //單獨處理snapshotlistener的方式
    private fun getData(): MutableLiveData<List<Post>> {
        val postData = FirebaseFirestore.getInstance()
            .collection("Posts")
            .orderBy("createdTime", Query.Direction.DESCENDING)
        postData.addSnapshotListener { snapshot, exception ->
            Timber.d("You are in HomeViewModel")

            exception?.let {
                Timber.d("Exception ${it.message}")
            }

            val list = mutableListOf<Post>()
            for (document in snapshot!!) {
                val post = document.toObject(Post::class.java)
                list.add(post)
            }

            liveData.value = list
        }
        return liveData
    }

    val display = MediatorLiveData<List<Post>>().apply {
        addSource(liveData) {
            it.let { posts ->
                value = if (tagSet.value == null) {
                    Timber.d("1 初始觀察到的所有貼文數量 ${posts?.size}")
                    posts
                } else {
                    val dataList = posts.filter { it ->
                        it.catalog == tagSet.value || it.catalog.isEmpty() || it.catalog.isBlank()
                    }
                    Timber.d("2 透過tagSet篩選之後給livedata資料 ${dataList.size}")
                    dataList
                }
            }
        }
        addSource(tagSet) {
            it.let { tag ->
                val dataList = liveData.value?.filter { it ->
                    it.catalog == tag
                }
                Timber.d("3 按下 $tag ${dataList?.size}")
                value = if (tag != null) {
                    dataList
                } else {
                    liveData.value
                }
            }
        }
    }

    fun changeTag (tagSet: String) {
        Timber.d("changeTag $tagSet")
        _tagSet.value = tagSet
    }

    fun resetTag () {
        _tagSet.value = null
    }

    fun navigateToDetail(item: Post) {
        _navigateToDetail.value = item
    }

    fun onDetailNavigated() {
        _navigateToDetail.value = null
    }

}
