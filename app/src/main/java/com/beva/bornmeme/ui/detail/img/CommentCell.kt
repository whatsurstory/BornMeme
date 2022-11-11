package com.beva.bornmeme.ui.detail.img

import com.beva.bornmeme.model.Comment

sealed class CommentCell{

    abstract val id:String

    data class ParentComment(val comment: Comment) : CommentCell() {
        override val id: String
            get() = comment.commentId
            var hasChild = false
    }
    data class ChildComment(val comment: Comment) : CommentCell() {
        override val id: String
            get() = comment.commentId
    }
}
