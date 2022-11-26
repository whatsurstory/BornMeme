package com.beva.bornmeme.ui.detail.img

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemDetailCommentChildBinding
import com.beva.bornmeme.databinding.ItemDetailCommentParentBinding
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.User
import com.bumptech.glide.Glide
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
            }
            binding.seeMoreBtn.setOnClickListener {
                uiState.onClickToSeeMore(item)
                binding.backBtn.visibility = View.VISIBLE
            }

            binding.backBtn.setOnClickListener {
                uiState.onClickToBack(item.id)
                item.hasChild = true
            }

            binding.replyBtn.setOnClickListener {
                uiState.onClickToReply(item)
                Timber.d("Observe replybtn ${item.comment.commentId}")
            }
            binding.commentLikeBtn.setOnClickListener {
                uiState.onClickToLike(item.comment.commentId)
            }
            binding.commentDislikeBtn.setOnClickListener {
                uiState.onClickToDislike(item.comment.commentId)
            }
            if (item.comment.like.isNullOrEmpty()) {
                binding.commentLikeNum.text = "0"
            } else {
                binding.commentLikeNum.text = item.comment.like.size.toString()
            }
            if (item.comment.dislike.isNullOrEmpty()) {
                binding.commentDislikeNum.text = "0"
            } else {
                binding.commentDislikeNum.text = item.comment.dislike.size.toString()
            }

            binding.commentZoneText.text = item.comment.content
//            val timeString = item.comment.time?.toDate()?.toString()
            val commentTime = item.comment.time?.toDate()?.time
            val currentTime = System.currentTimeMillis()
            Timber.d("currentTime $currentTime")
            val seconds = (currentTime - commentTime!!) / 1000
            val minutes = seconds / 60
            val hour = minutes / 60
            val day = hour / 24
            if (seconds <= 50) {
                binding.commentTime.text = "$seconds seconds ago"
            } else if (seconds in 60..3600 ) {
                binding.commentTime.text = "$minutes minutes ago"
            } else if (seconds in 3600..86400) {
                binding.commentTime.text = "$hour hour ago"
            } else if (seconds >= 86400) {
                binding.commentTime.text = "$day days ago"
            }
            Timber.d("秒 $seconds 分 $minutes 時 $hour 天 $day")

            uiState.getUserImg(item.comment.userId) { user: User ->
                Timber.d("img => ${user.profilePhoto}")
                Glide.with(binding.commentUserImg)
                    .load(user.profilePhoto)
                    .placeholder(R.drawable._50)
                    .into(binding.commentUserImg)
                binding.commentUserName.text = user.userName
            }
        }
    }

    class ChildViewHolder(private var binding: ItemDetailCommentChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: CommentCell.ChildComment, uiState: ImgDetailViewModel.UiState) {

            Timber.d("ChildViewHolder $adapterPosition")
            binding.childDislikeBtn.setOnClickListener {
                uiState.onClickToDislike(item.comment.commentId)
            }
            binding.childLikeBtn.setOnClickListener {
                uiState.onClickToLike(item.comment.commentId)
            }
            if (item.comment.like.isNullOrEmpty()) {
                binding.childLikeNum.text = "0"
            } else {
                binding.childLikeNum.text = item.comment.like.size.toString()
            }
            if (item.comment.dislike.isNullOrEmpty()){
                binding.childDislikeNum.text = "0"
            } else {
                binding.childDislikeNum.text = item.comment.dislike.size.toString()
            }
            binding.childZoneText.text = item.comment.content
//            val timeString = item.comment.time?.toDate()?.toString()
            val commentTime = item.comment.time?.toDate()?.time
            val currentTime = System.currentTimeMillis()
            val seconds = (currentTime - commentTime!!)/1000
            val minutes = seconds / 60
            val hour = minutes / 60
            val day = hour / 24
            if (seconds <= 50) {
                binding.childTime.text = "$seconds seconds ago"
            } else if (seconds in 60..3600 ) {
                binding.childTime.text = "$minutes minutes ago"
            } else if (seconds in 3600..86400) {
                binding.childTime.text = "$hour hour ago"
            } else if (seconds >= 86400) {
                binding.childTime.text = "$day days ago"
            }
            Timber.d("秒 $seconds 分 $minutes 時 $hour 天 $day")

            uiState.getUserImg(item.comment.userId) { user: User ->
                Timber.d("img => ${user.profilePhoto}")
                Glide.with(binding.childUserImg)
                    .load(user.profilePhoto)
                    .placeholder(R.drawable._50)
                    .into(binding.childUserImg)
                binding.childUserName.text = user.userName
            }
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