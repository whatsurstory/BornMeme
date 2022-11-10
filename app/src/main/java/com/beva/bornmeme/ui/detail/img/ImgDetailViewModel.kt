package com.beva.bornmeme.ui.detail.img

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.beva.bornmeme.model.Comment
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.checkerframework.checker.units.qual.C
import timber.log.Timber



class ImgDetailViewModel : ViewModel() {

data class UiState (
    val onClickToReply: () -> Unit,
    val onClickToLike: (comment: CommentCell.ParentComment) -> Unit,
    val onClickToDislike: (comment: CommentCell.ParentComment) -> Unit,
    val onClickToSeeMore: (comment: CommentCell.ParentComment) -> Unit,
    val onClickToBack: (commentId: String)-> Unit

)
    val liveData = MutableLiveData<List<Comment>>()

    val uiState = UiState(
        {
            //TODO:navigate to comment dialog
        },
        { comment ->
            //TODO:saving userId render the view of like
        },
        { comment ->
            //TODO:saving userId render the view of dislike
        },
        { comment ->
            val children = liveData.value?.filter {
                it.parentId == comment.parent.commentId
            }

            var index = commentCells.value?.indexOf(comment) ?: 0

            if (children?.isNotEmpty() == true) {

                for (item in children) {
                    val child = CommentCell.ChildComment(item)
                    index++
                    commentCells.value?.add(index, child)
                }
            }
            commentCells.value = commentCells.value
            comment.hasChild = false
        },
        { parentId ->
            //using filter: let the element condition which is fit to display
            commentCells.value = commentCells.value?.filter {
                it is CommentCell.ParentComment
                        || (it is CommentCell.ChildComment && it.child.parentId != parentId)
            }?.toMutableList()

        }
    )


    val commentCells = MutableLiveData<MutableList<CommentCell>>(mutableListOf())

    fun initCells(comments: List<Comment>) {
//using condition classify comments cell is parent or not
//        for (item in it) {
//            if (item.parentId.isEmpty()) {
//                val parent = CommentCell.ParentComment(item)
//                cells.add(parent)
//                Timber.d("parent cells $cells")
//            } else {
//                val child = CommentCell.ChildComment(item)
//                cells.add(child)
//                Timber.d("child cells $cells")
//                }
//            }
//using the filter to classify
//        val children = comments.filter { !it.parentId.isNullOrEmpty() }
        val parents = comments.filter { it.parentId.isNullOrEmpty() }
        val cells = mutableListOf<CommentCell>()
        for (item in parents) {
            val parent = CommentCell.ParentComment(item)
            cells.add(parent)
            Timber.d("Check viewModel $cells")

            //we get the comments has parent or not from here
            val subComments = comments.filter { it.parentId == item.commentId }
            parent.hasChild = subComments.isNotEmpty()
//                for (childItem in children){
//                    val child = CommentCell.ChildComment(childItem)
//                    if (childItem.parentId == parent.id){
//                        cells.add(child)
//                    }
//                }
        }
        commentCells.value = cells

            }

    fun getComments(postId: String): MutableLiveData<List<Comment>> {

        Firebase.firestore.collection("Comments")
            .whereEqualTo("postId", postId)
            .addSnapshotListener {  querySnapshot, firebaseFirestoreException ->
                Timber.d("check Data $postId")
                firebaseFirestoreException?.let {
                    Timber.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                }
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