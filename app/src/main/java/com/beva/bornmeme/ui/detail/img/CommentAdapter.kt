package com.beva.bornmeme.ui.detail.img

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.databinding.ItemDetailCommentChildBinding
import com.beva.bornmeme.databinding.ItemDetailCommentParentBinding
import com.google.common.base.Strings.isNullOrEmpty
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class CommentAdapter(private val uiState: ImgDetailViewModel.UiState): ListAdapter<CommentCell, RecyclerView.ViewHolder>(DiffCallback) {

    class ParentViewHolder(private var binding: ItemDetailCommentParentBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: CommentCell.ParentComment, uiState: ImgDetailViewModel.UiState) {
            if (item.hasChild) {
                binding.seeMoreBtn.visibility = View.VISIBLE
                binding.backBtn.visibility = View.GONE
            } else {
                binding.seeMoreBtn.visibility = View.GONE
                binding.backBtn.visibility = View.VISIBLE
            }
            binding.seeMoreBtn.setOnClickListener {
                uiState.onClickToSeeMore(item)
            }

            binding.backBtn.setOnClickListener {
                uiState.onClickToBack(item.id)
                item.hasChild = true
            }

            binding.replyBtn.setOnClickListener {
                uiState.onClickToReply()
            }
            binding.commentLikeBtn.setOnClickListener {
                uiState.onClickToLike(item)
            }
            binding.commentDislikeBtn.setOnClickListener {
                uiState.onClickToDislike(item)
            }
            if (item.parent.like.isNullOrEmpty()){
                binding.commentLikeNum.text = "0"
            } else {
                binding.commentLikeNum.text = item.parent.like.size.toString()
            }
            if (item.parent.dislike.isNullOrEmpty()){
                binding.commentDislikeNum.text = "0"
            } else {
                binding.commentDislikeNum.text = item.parent.dislike.size.toString()
            }

            binding.commentZoneText.text = item.parent.content
            binding.commentUserName.text = item.parent.userId
            val timeString = item.parent.time?.toDate()?.toString()
            val commentTime = item.parent.time?.toDate()?.time
            val currentTime = System.currentTimeMillis()
            val seconds = (currentTime - commentTime!!)/1000
            val minutes = seconds / 60
            val hour = minutes / 60
            val day = hour / 24
            Timber.d("秒 $seconds 分 $minutes 時 $hour 天 $day")
            binding.commentTime.text = "$day days ago"
        }
    }

    class ChildViewHolder(private var binding: ItemDetailCommentChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: CommentCell.ChildComment, uiState: ImgDetailViewModel.UiState) {

            Timber.d("ChildViewHolder $adapterPosition")
            binding.childDislikeBtn.setOnClickListener {
                uiState.onClickToDislike
            }
            binding.childLikeBtn.setOnClickListener {
                uiState.onClickToLike
            }
            if (item.child.like.isNullOrEmpty()) {
                binding.childLikeNum.text = "0"
            } else {
                binding.childLikeNum.text = item.child.like.size.toString()
            }
            if (item.child.dislike.isNullOrEmpty()){
                binding.childDislikeNum.text = "0"
            } else {
                binding.childDislikeNum.text = item.child.dislike.size.toString()
            }
            binding.childZoneText.text = item.child.content
            binding.childUserName.text = item.child.userId
            //TODO: user data need query to show the image and name
            //error handle: the time need checking day or hours or minute
            val timeString = item.child.time?.toDate()?.toString()
            val commentTime = item.child.time?.toDate()?.time
            val currentTime = System.currentTimeMillis()
            val seconds = (currentTime - commentTime!!)/1000
            val minutes = seconds / 60
            val hour = minutes / 60
            val day = hour / 24
            Timber.d("秒 $seconds 分 $minutes 時 $hour 天 $day")
            binding.childTime.text = "$day days ago"
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
            is ParentViewHolder -> {
                val data = getItem(position) as CommentCell.ParentComment
                holder.bind(data, uiState)
                Timber.d("data $data position $position")
            }
            is ChildViewHolder -> {
                val data = getItem(position) as CommentCell.ChildComment
                holder.bind(data, uiState)
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