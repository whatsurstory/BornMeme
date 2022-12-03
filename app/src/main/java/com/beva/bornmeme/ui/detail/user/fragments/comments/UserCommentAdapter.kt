package com.beva.bornmeme.ui.detail.user.fragments.comments

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemUserCommentBinding
import com.beva.bornmeme.databinding.ItemUserPostsBinding
import com.beva.bornmeme.model.Comment
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.UserManager.user
import com.bumptech.glide.Glide
import timber.log.Timber
import java.sql.Date
import java.text.ParsePosition

class UserCommentAdapter(private val uiState: CommentsViewModel.UiState): ListAdapter<Comment, UserCommentAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemUserCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bind(item: Comment, uiState: CommentsViewModel.UiState){
            binding.contentText.text = item.content
            val timeString = item.time?.toDate()?.toLocaleString()
            binding.timeText.text =timeString
            uiState.getPostImg(item.postId) { post:Post ->
                Timber.d("img => ${post.url}")
                Glide.with(binding.commentImg)
                    .load(post.url)
                    .placeholder(R.drawable.place_holder)
                    .into(binding.commentImg)
            }
        }
    }


    companion object DiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return (oldItem == newItem)
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemUserCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), uiState)
    }
}

class Timestamp {
    /**
     * Timestamp to String
     * @param Timestamp
     * @return String
     */
    @SuppressLint("SimpleDateFormat")
    fun transToString(time:Long):String{
        return SimpleDateFormat("YY-MM-DD-hh-mm-ss").format(time)
    }

    /**
     * String to Timestamp
     * @param String
     * @return Timestamp
     */

    @SuppressLint("SimpleDateFormat")
    fun transToTimeStamp(date:String):Long{
        return SimpleDateFormat("YY-MM-DD-hh-mm-ss").parse(date, ParsePosition(0)).time
    }
}