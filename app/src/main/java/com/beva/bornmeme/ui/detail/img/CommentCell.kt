package com.beva.bornmeme.ui.detail.img

import com.beva.bornmeme.model.Comment

sealed class CommentCell{

    abstract val id:String

    data class ParentComment(val parent: Comment) : CommentCell() {
        override val id: String
            get() = parent.commentId
            var hasChild = false
    }
    data class ChildComment(val child: Comment) : CommentCell() {
        override val id: String
            get() = child.commentId
    }
}
