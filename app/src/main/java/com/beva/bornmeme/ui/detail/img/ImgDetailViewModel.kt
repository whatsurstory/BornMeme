package com.beva.bornmeme.ui.detail.img

import android.view.View
import android.widget.TextView
import androidx.collection.arrayMapOf
import androidx.lifecycle.*
import com.beva.bornmeme.databinding.FragmentImgDetailBinding
import com.beva.bornmeme.model.*
import com.beva.bornmeme.model.UserManager.user
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber



class ImgDetailViewModel(postOwnerId: String) : ViewModel() {

data class UiState (
    val onClickToReply: (comment:CommentCell.ParentComment) -> String,
    val onClickToLike: (comment: String) -> Unit,
    val onClickToDislike: (comment: String) -> Unit,
    val onClickToSeeMore: (comment: CommentCell.ParentComment) -> Unit,
    val onClickToBack: (commentId: String)-> Unit,
    val getUserImg: (userId:String, onUserObtained: ((User) -> Unit)) -> Unit
)
    val liveData = MutableLiveData<List<Comment>>()

    private val _navigate2Comment = MutableLiveData<CommentCell.ParentComment>()
    val navigate2Comment: LiveData<CommentCell.ParentComment>
        get() = _navigate2Comment

    val uiState = UiState(

        onClickToReply = {
            return@UiState navigate2Comment(it).toString()
        },
        { comment ->
            val collection =FirebaseFirestore.getInstance().collection("Comments")

            collection.whereEqualTo("like", UserManager.user.userId)
                .addSnapshotListener { snapshot, e ->
                    collection.document(comment).update("dislike", FieldValue.arrayRemove(UserManager.user.userId))
                        .addOnSuccessListener {
                            collection.document(comment)
                                .update("like", FieldValue.arrayUnion(UserManager.user.userId))
                                .addOnSuccessListener {
                                    Timber.d("Success add comment like $comment")
                                }.addOnFailureListener {
                                    Timber.d("Error ${it.message}")
                                }
                        }
                }
        },
        { comment ->
            val collection =FirebaseFirestore.getInstance().collection("Comments")

            collection.whereEqualTo("like", UserManager.user.userId)
                .addSnapshotListener { snapshot, e ->
                    collection.document(comment).update("like", FieldValue.arrayRemove(UserManager.user.userId))
                        .addOnSuccessListener {
                        collection.document(comment)
                            .update("dislike", FieldValue.arrayUnion(UserManager.user.userId))
                            .addOnSuccessListener {
                                Timber.d("Success add comment dislike $comment")
                            }.addOnFailureListener {
                                Timber.d("Error ${it.message}")
                            }
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
        getUserImg = { userId, onUserObtained ->
            Firebase.firestore
                .collection("Users")
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
                if (querySnapshot != null) {
                    for (document in querySnapshot) {
                        Timber.d("check data  ${document.id} => ${document.data}")
                        val commentList = document.toObject(Comment::class.java)
                        list.add(commentList)
                        Timber.d("check $list")
                    }
                }
                liveData.value = list
            }
        return liveData
    }
    private fun navigate2Comment(id: CommentCell.ParentComment) {
        _navigate2Comment.value = id
    }

    fun onDetailNavigated() {
        _navigate2Comment.value = null
    }

    fun doneCollection(postId: String){
        Firebase.firestore.collection("Posts")
            .document(postId)
            .update("collection",FieldValue.arrayUnion(UserManager.user.userId))
            .addOnSuccessListener {
                Timber.d("Success Posts adding User ID")
            }.addOnFailureListener {
                Timber.d("ERROR ${it.message}")
            }
    }

    fun onClickCollection(title:String, postId: String, url: String) {
        val ref = Firebase.firestore
            .collection("Users")
            .document(UserManager.user.userId!!)
            .collection("Folders")
            .document(title)

        Firebase.firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val list = arrayMapOf("id" to postId, "url" to url)
            Timber.d("snapshot ??? $snapshot")
            if (snapshot.data != null) {
                Timber.d("snapshot.data != null")
                transaction.update(ref,
                    "posts", FieldValue.arrayUnion(list))
            } else {
                Timber.d("snapshot.data == null")
                transaction.set(ref, hashMapOf(
                    "name" to ref.id,
                    "createTime" to Timestamp.now(),
                    "posts" to FieldValue.arrayUnion(list))
                )
            }
        }.addOnSuccessListener {
            Timber.d("Success to adding $ref")

        }.addOnFailureListener {
            Timber.d("ERROR ${it.message}")
        }
    }


    init {
        getUser(postOwnerId)
    }

    val userData = MutableLiveData<List<User>>()

    private fun getUser(postOwnerId: String): MutableLiveData<List<User>> {
        Firebase.firestore.collection("Users")
            .whereEqualTo("userId", postOwnerId)
            .addSnapshotListener {  querySnapshot, firebaseFirestoreException ->

                firebaseFirestoreException?.let {
                    Timber.w("[${this::class.simpleName}] Error getting documents. ${it.message}")
                }

                val list = mutableListOf<User>()
                for (document in querySnapshot!!) {
                    Timber.d("check User in Post -> ${document.id} => ${document.data}")
                    val userData = document.toObject(User::class.java)
                    list.add(userData)
                    Timber.d("check $list")
                }
                userData.value = list
            }
        return userData
    }
//
    val folderData = MutableLiveData<List<String>>()

    fun getFolder() {
        Firebase.firestore.collection("Users")
            .document(UserManager.user.userId!!)
            .collection("Folders")
            .get()
            .addOnCompleteListener { task ->
                val list = mutableListOf<String>()
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        list.add(document.id)
                        Timber.d("data ${document.id}")
                    }
                    folderData.value = list
                    } else {
                    Timber.d(task.exception?.message!!)
                    }
                }
            }
        }
