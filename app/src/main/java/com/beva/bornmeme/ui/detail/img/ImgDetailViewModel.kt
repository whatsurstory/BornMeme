package com.beva.bornmeme.ui.detail.img

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Comment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class ImgDetailViewModel : ViewModel() {

    private val liveData = MutableLiveData<List<Comment>>()

    val commentCells = Transformations.map(liveData) {
        val cells = mutableListOf<CommentCell>()
        for (item in it){
            if (item.parentId.isNotEmpty()){
                val child = CommentCell.ChildComment(item)
                cells.add(child)
                Timber.d("child cells $cells")
            } else {
                val parent = CommentCell.ParentComment(item)
                cells.add(parent)
                Timber.d("parent cells $cells")
            }
        }
        cells
//        Timber.d("Check viewModel $cells")
    }

    fun getComments(postId: String): MutableLiveData<List<Comment>> {

        Firebase.firestore.collection("Comments")
            .whereEqualTo("postId", postId)
            .addSnapshotListener {  querySnapshot, firebaseFirestoreException ->
                Timber.d("check Data $postId")
                val list = mutableListOf<Comment>()
                for (document in querySnapshot!!) {
                    Timber.d("check data  ${document.id} => ${document.data}")
                    val commentList = document.toObject(Comment::class.java)
                    list.add(commentList)
                    Timber.d("check $list")
                }
                liveData.value = list
            }
        return liveData
    }

}