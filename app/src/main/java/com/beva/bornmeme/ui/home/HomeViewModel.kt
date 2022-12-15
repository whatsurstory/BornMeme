package com.beva.bornmeme.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.os.Build.VERSION_CODES.M
import android.util.Log
import androidx.lifecycle.*
import com.beva.bornmeme.R
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.Resource
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.beva.bornmeme.ui.detail.img.CommentCell
import com.beva.bornmeme.ui.detail.img.ImgDetailViewModel
import com.bumptech.glide.Glide.init
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.callbackFlow
import org.checkerframework.checker.units.qual.s
import timber.log.Timber

class HomeViewModel(context: Context) : ViewModel() {

    data class UiState(
        val getUserImg: (userId: String, onUserObtained: ((User) -> Unit)) -> Unit
    )

    val liveData = MutableLiveData<List<Post>>()

    private var _tagSet = MutableLiveData<String>()
    private val tagSet: MutableLiveData<String>
        get() = _tagSet

    // Handle navigation to detail
    private val _navigateToDetail = MutableLiveData<Post>()

    val navigateToDetail: LiveData<Post>
        get() = _navigateToDetail

    val uiState = UiState(
        getUserImg = { userId, onUserObtained ->
            Firebase.firestore
                .collection("Users")
                .document(userId)
                .get().addOnCompleteListener {
                    val user = it.result.toObject(User::class.java)
                    if (user != null) {
                        return@addOnCompleteListener onUserObtained(user)
                    }
                }
        }
    )


    init {
        getData(context)
    }

    val tagCell = Transformations.map(liveData) {
        val cells = mutableListOf<String>()
        for (item in it) {
            val tag = item.catalog
            //filter the specific text
            if (!cells.contains(tag)) {
                cells.add(tag)
            }
        }
        cells
    }

    private fun getData(context: Context): MutableLiveData<List<Post>> {
        val postData = FirebaseFirestore.getInstance()
            .collection(context.getString(R.string.post_collection_text))
            .orderBy("createdTime", Query.Direction.DESCENDING)
        postData.addSnapshotListener { snapshot, exception ->
            Timber.d("You are in HomeViewModel")
            exception?.let {
                Timber.d("Exception ${it.message}")
            }

            val list = mutableListOf<Post>()
            if (snapshot != null) {
                for (document in snapshot) {
                    val post = document.toObject(Post::class.java)
                    list.add(post)
                }
            }
            liveData.value = list
        }
        return liveData
    }

    val postData = MediatorLiveData<List<Post>>().apply {
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

    fun changeTag(tagSet: String) {
        _tagSet.value = tagSet
    }

    fun resetTag() {
        _tagSet.value = null
    }

    fun navigateToDetail(item: Post) {
        _navigateToDetail.value = item
    }

    fun onDetailNavigated() {
        _navigateToDetail.value = null
    }
}
//Todo
//Extension function of block
fun List<Post>.filterBlock(): List<Post> {
    return this.filter { !UserManager.user.blockList.contains(it.ownerId) }
}
