package com.beva.bornmeme.ui.detail.img

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.databinding.ItemDetailCommentChildBinding
import com.beva.bornmeme.databinding.ItemDetailCommentParentBinding
import com.beva.bornmeme.model.Comments
import java.sql.Date

class CommentAdapter: ListAdapter<CommentCell, RecyclerView.ViewHolder>(DiffCallback) {

    class ParentViewHolder(private var binding: ItemDetailCommentParentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Comments) {
            binding.commentLikeBtn.setOnClickListener {
                //回傳點擊的userId
            }
            binding.commentDislikeBtn.setOnClickListener {
                //回傳點擊的userId
            }
            binding.seeMoreBtn.setOnClickListener {
                //call 有parentID的comment
            }
            binding.commentLikeNum.text = item.like.size.toString()
            binding.commentTime.text = Date(item.time!!).toString()

            binding.commentZoneText.text = item.content
            binding.commentDislikeNum.text = item.dislike.size.toString()

            binding.commentUserImg //Glide
            binding.commentUserName//

        }
    }

    class ChildViewHolder(private var binding: ItemDetailCommentChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Comments) {
            binding.childDislikeBtn.setOnClickListener {
                //回傳點擊的userId
            }
            binding.childLikeBtn.setOnClickListener {
                //回傳點擊的userId
            }
            binding.childDislikeNum.text = item.dislike.size.toString()
            binding.childLikeNum.text = item.like.size.toString()
            binding.childUserImg
            binding.childUserName
            binding.childZoneText.text = item.content
            binding.childTime.text = Date(item.time!!).toString()
        }
    }


    companion object DiffCallback : DiffUtil.ItemCallback<CommentCell>() {
        override fun areItemsTheSame(oldItem: CommentCell, newItem: CommentCell): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: CommentCell, newItem: CommentCell): Boolean {
            return oldItem.id == newItem.id
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
                holder.bind((getItem(position) as CommentCell.ParentComment).parent)
            }
            is ChildViewHolder -> {
                holder.bind((getItem(position) as CommentCell.ChildComment).child)
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