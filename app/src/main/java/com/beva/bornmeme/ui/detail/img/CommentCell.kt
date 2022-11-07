package com.beva.bornmeme.ui.detail.img

import com.beva.bornmeme.model.Comments

sealed class CommentCell{

    abstract val id:String

    data class ParentComment(val parent: Comments) : CommentCell() {
        override val id: String
            get() = parent.commentId
    }
    data class ChildComment(val child: Comments) : CommentCell() {
        override val id: String
            get() = child.commentId
    }
}
