package com.beva.bornmeme.ui.detail.img

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color.parseColor
import android.transition.TransitionInflater.from
import android.view.Gravity
import android.view.LayoutInflater
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.collection.arrayMapOf
import androidx.core.app.TaskStackBuilder.from
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.transition.TransitionInflater.from
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentImgDetailBinding
import com.beva.bornmeme.databinding.SnackBarCustomBinding
import com.beva.bornmeme.model.*
import com.beva.bornmeme.model.UserManager.user
import com.beva.bornmeme.ui.home.filterBlock
import com.bumptech.glide.Glide.init
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


class ImgDetailViewModel(postOwnerId: String, application: Application?) : ViewModel() {

    data class UiState(
        val onClickToReply: (comment: CommentCell.ParentComment) -> String,
        val onClickToLike: (comment: String, context:Context) -> Unit,
        val onClickToDislike: (comment: String, context:Context) -> Unit,
        val onClickToSeeMore: (comment: CommentCell.ParentComment) -> Unit,
        val onClickToBack: (commentId: String) -> Unit,
        val getUserImg: (context:Context,userId: String, onUserObtained: ((User) -> Unit)) -> Unit
    )

    val liveData = MutableLiveData<List<Comment>>()

    private val _navigate2Comment = MutableLiveData<CommentCell.ParentComment>()
    val navigate2Comment: LiveData<CommentCell.ParentComment>
        get() = _navigate2Comment

    val uiState = UiState(

        onClickToReply = {
            return@UiState navigate2Comment(it).toString()
        },
        { comment, context ->
            val collection = FirebaseFirestore.getInstance()
                .collection(context.getString(R.string.comment_collection_text))

            collection.whereEqualTo("like", UserManager.user.userId)
                .addSnapshotListener { snapshot, e ->
                    collection.document(comment)
                        .update("dislike", FieldValue.arrayRemove(UserManager.user.userId))
                        .addOnSuccessListener {
                            collection.document(comment)
                                .update("like", FieldValue.arrayUnion(UserManager.user.userId))
//                                .addOnSuccessListener {
//                                    Timber.d("Success add comment like $comment")
//                                }.addOnFailureListener {
//                                    Timber.d("Error ${it.message}")
//                                }
                        }
                }
        },
        { comment, context ->
            val collection = FirebaseFirestore.getInstance()
                .collection(context.getString(R.string.comment_collection_text))

            collection.whereEqualTo("like", UserManager.user.userId)
                .addSnapshotListener { snapshot, e ->
                    collection.document(comment)
                        .update("like", FieldValue.arrayRemove(UserManager.user.userId))
                        .addOnSuccessListener {
                            collection.document(comment)
                                .update("dislike", FieldValue.arrayUnion(UserManager.user.userId))
//                                .addOnSuccessListener {
//                                    Timber.d("Success add comment dislike $comment")
//                                }.addOnFailureListener {
//                                    Timber.d("Error ${it.message}")
//                                }
                        }
                }
        },
        { comment ->
            val children = liveData.value?.filter {
                it.parentId == comment.comment.commentId
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
                        || (it is CommentCell.ChildComment && it.comment.parentId != parentId)
            }?.toMutableList()

        },
        getUserImg = {context, userId, onUserObtained ->
            Firebase.firestore
                .collection(context.getString(R.string.user_collection_text))
                .document(userId)
                .get()
                .addOnCompleteListener {
                    val post = it.result.toObject(User::class.java)
                    if (post != null) {
                        return@addOnCompleteListener onUserObtained(post)
                    }
                }
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
//            Timber.d("Check viewModel $cells")

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

    fun getComments(postId: String, application: Application?): MutableLiveData<List<Comment>> {

        application?.let { app ->
            Firebase.firestore.collection(app.getString(R.string.comment_collection_text))
                .whereEqualTo("postId", postId)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
    //                Timber.d("check Data $postId")
                    firebaseFirestoreException?.let {
                        Timber.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                    }
                    val list = mutableListOf<Comment>()
                    if (querySnapshot != null) {
                        for (document in querySnapshot) {
    //                        Timber.d("check data  ${document.id} => ${document.data}")
                            val commentList = document.toObject(Comment::class.java)
                            list.add(commentList)
    //                        Timber.d("check $list")
                        }
                    }
                    liveData.value = list
                }
        }
        return liveData
    }

    private fun navigate2Comment(id: CommentCell.ParentComment) {
        _navigate2Comment.value = id
    }

    fun onDetailNavigated() {
        _navigate2Comment.value = null
    }

    fun doneCollection(postId: String, application: Application?) {
        application?.let { app ->
            Firebase.firestore.collection(app.getString(R.string.post_collection_text))
                .document(postId)
                .update("collection", FieldValue.arrayUnion(UserManager.user.userId))
        }
//            .addOnSuccessListener {
//                Timber.d("Success Posts adding User ID")
//            }.addOnFailureListener {
//                Timber.d("ERROR ${it.message}")
//            }
    }

    fun onClickCollection(application: Application?, title: String, postId: String, url: String) {
        val ref = UserManager.user.userId?.let {
            application?.let { app ->
                Firebase.firestore
                    .collection(app.getString(R.string.user_collection_text))
                    .document(it)
                    .collection(app.getString(R.string.folder_collection_text))
                    .document(title)
            }
        }

        Firebase.firestore.runTransaction { transaction ->
            val snapshot = ref?.let { transaction.get(it) }
            val list = arrayMapOf("id" to postId, "url" to url)
//            Timber.d("snapshot ??? $snapshot")
            if (snapshot?.data != null) {
                transaction.update(
                    ref,
                    "posts", FieldValue.arrayUnion(list)
                )
            } else {
                ref?.let {
                    transaction.set(
                        it, hashMapOf(
                            "name" to ref.id,
                            "createTime" to Timestamp.now(),
                            "posts" to FieldValue.arrayUnion(list)
                        )
                    )
                }
            }
        }
    }

    init {
        getUser(postOwnerId, application)
    }

    val userData = MutableLiveData<List<User>>()

    private fun getUser(postOwnerId: String, application: Application?): MutableLiveData<List<User>> {
        application?.let { app ->
            Firebase.firestore.collection(app.getString(R.string.user_collection_text))
                .whereEqualTo("userId", postOwnerId)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    firebaseFirestoreException?.let {
                        Timber.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                    }

                    val list = mutableListOf<User>()
                    for (document in querySnapshot!!) {
    //                    Timber.d("check User in Post -> ${document.id} => ${document.data}")
                        val userData = document.toObject(User::class.java)
                        list.add(userData)
    //                    Timber.d("check $list")
                    }
                    userData.value = list
                }
        }
        return userData
    }

    val folderData = MutableLiveData<List<String>>()

    fun getFolder(application: Application?) {
        UserManager.user.userId?.let {
            application?.let { app ->
                Firebase.firestore.collection(app.getString(R.string.user_collection_text))
                    .document(it)
                    .collection(app.getString(R.string.folder_collection_text))
                    .get()
                    .addOnCompleteListener { task ->
                        val list = mutableListOf<String>()
                        if (task.isSuccessful) {
                            for (document in task.result) {
                                list.add(document.id)
        //                            Timber.d("data ${document.id}")
                            }
                            folderData.value = list
                        } else {
                            Timber.d(task.exception?.message)
                        }
                    }
            }
        }
    }

    fun navigate2UserDetail(fragment: ImgDetailFragment, userId: String) {
        findNavController(fragment).navigate(
            MobileNavigationDirections
                .navigateToUserDetailFragment(userId)
        )
    }
}

fun List<Comment>.filterBlock(): List<Comment> {
    return this.filter { !UserManager.user.blockList.contains(it.userId) }
}
