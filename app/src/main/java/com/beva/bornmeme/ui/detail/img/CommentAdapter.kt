package com.beva.bornmeme.ui.detail.img

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.databinding.ItemDetailCommentChildBinding
import com.beva.bornmeme.databinding.ItemDetailCommentParentBinding
import com.beva.bornmeme.model.Comment

class CommentAdapter: ListAdapter<CommentCell, RecyclerView.ViewHolder>(DiffCallback) {

    class ParentViewHolder(private var binding: ItemDetailCommentParentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommentCell.ParentComment) {
            binding.commentLikeBtn.setOnClickListener {
                //回傳點擊的userId
            }
            binding.commentDislikeBtn.setOnClickListener {
                //回傳點擊的userId
            }
            binding.seeMoreBtn.setOnClickListener {
                //call 有parentID的comment
            }
            binding.commentLikeNum.text = item.parent.like.size.toString()
            binding.commentZoneText.text = item.parent.content
            binding.commentDislikeNum.text = item.parent.dislike.size.toString()

            binding.commentUserImg //Glide
            binding.commentUserName//

        }
    }

    class ChildViewHolder(private var binding: ItemDetailCommentChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommentCell.ChildComment) {
            binding.childDislikeBtn.setOnClickListener {
                //回傳點擊的userId
            }
            binding.childLikeBtn.setOnClickListener {
                //回傳點擊的userId
            }
            binding.childDislikeNum.text = item.child.dislike.size.toString()
            binding.childLikeNum.text = item.child.like.size.toString()
            binding.childZoneText.text = item.child.content

            binding.childUserImg
            binding.childUserName
        }
    }


    companion object DiffCallback : DiffUtil.ItemCallback<CommentCell>() {
        override fun areItemsTheSame(oldItem: CommentCell, newItem: CommentCell): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: CommentCell, newItem: CommentCell): Boolean {
            return oldItem == newItem
        }

        private const val ITEM_VIEW_TYPE_PARENT = 0x00
        private const val ITEM_VIEW_TYPE_CHILD = 0x01
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_PARENT -> ParentViewHolder(
                ItemDetailCommentParentBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            ITEM_VIEW_TYPE_CHILD -> ChildViewHolder(
                ItemDetailCommentChildBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is ParentViewHolder -> {
                val data = getItem(position) as CommentCell.ParentComment
                holder.bind(data)
            }
            is ChildViewHolder -> {
                val data = getItem(position) as CommentCell.ChildComment
                holder.bind(data)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CommentCell.ParentComment -> ITEM_VIEW_TYPE_PARENT
            is CommentCell.ChildComment -> ITEM_VIEW_TYPE_CHILD
        }
    }

}