package com.beva.bornmeme.ui.detail.img

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.databinding.ItemDetailCommentChildBinding
import com.beva.bornmeme.databinding.ItemDetailCommentParentBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class CommentAdapter(private val uiState: ImgDetailViewModel.UiState): ListAdapter<CommentCell, RecyclerView.ViewHolder>(DiffCallback) {

    class ParentViewHolder(private var binding: ItemDetailCommentParentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommentCell.ParentComment, uiState: ImgDetailViewModel.UiState) {
            //binding.seeMoreBtn.visibility = View.VISIBLE
            binding.seeMoreBtn.setOnClickListener {
                uiState.onClickToSeeMore(item)
            }
            // binding.backBtn.visibility = View.VISIBLE
            binding.backBtn.setOnClickListener {
                uiState.onClickToBack(item.id)
            }

            binding.replyBtn.setOnClickListener {
                uiState.onClickToReply
            }
            binding.commentLikeBtn.setOnClickListener {
                uiState.onClickToLike
            }
            binding.commentDislikeBtn.setOnClickListener {
                uiState.onClickToDislike
            }
            binding.commentLikeNum.text = item.parent.like.size.toString()
            binding.commentZoneText.text = item.parent.content
            binding.commentDislikeNum.text = item.parent.dislike.size.toString()
            binding.commentUserName.text = item.parent.parentId
//            binding.commentUserImg //Glide

        }
    }

    class ChildViewHolder(private var binding: ItemDetailCommentChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommentCell.ChildComment) {

            Timber.d("ChildViewHolder $adapterPosition")
            binding.childDislikeBtn.setOnClickListener {
                //回傳點擊的userId
            }
            binding.childLikeBtn.setOnClickListener {
                //回傳點擊的userId
            }
            binding.childDislikeNum.text = item.child.dislike.size.toString()
            binding.childLikeNum.text = item.child.like.size.toString()
            binding.childZoneText.text = item.child.content
            binding.childUserName.text = item.child.commentId


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
            ITEM_VIEW_TYPE_PARENT -> CommentAdapter.ParentViewHolder(
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
            is CommentAdapter.ParentViewHolder -> {
                val data = getItem(position) as CommentCell.ParentComment
                holder.bind(data, uiState)
                Timber.d("data $data position $position")
            }
            is ChildViewHolder -> {
                val data = getItem(position) as CommentCell.ChildComment
                holder.bind(data)
                Timber.d("data $data position $position")
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