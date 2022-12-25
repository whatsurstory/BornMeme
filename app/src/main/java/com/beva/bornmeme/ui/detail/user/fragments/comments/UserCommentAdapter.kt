package com.beva.bornmeme.ui.detail.user.fragments.comments

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemUserCommentBinding
import com.beva.bornmeme.databinding.ItemUserPostsBinding
import com.beva.bornmeme.loadImage
import com.beva.bornmeme.model.Comment
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.UserManager
import com.beva.bornmeme.model.UserManager.user
import com.bumptech.glide.Glide
import timber.log.Timber
import java.sql.Date
import java.text.ParsePosition

class UserCommentAdapter(private val uiState: CommentsViewModel.UiState,
                         val context: Context) :
    ListAdapter<Comment, UserCommentAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemUserCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bind(item: Comment,
                 uiState: CommentsViewModel.UiState,
                 context: Context) {
            binding.contentText.text = item.content
            val timeString = item.time?.toDate()?.toLocaleString()
            binding.timeText.text = timeString
            uiState.getPostImg(context, item.postId) { post: Post ->
                binding.commentImg.loadImage(post.url)
            }
            if (item.userId == UserManager.user.userId) {
                binding.deleteCommentButton.visibility = View.VISIBLE

                binding.deleteCommentButton.setOnLongClickListener {
                    val builder: AlertDialog.Builder =
                        AlertDialog.Builder(it.context, R.style.AlertDialogTheme)
                    builder.setTitle(context.getString(R.string.remind_delete_text))
                    builder.setMessage(context.getString(R.string.check_delete_text) + item.content)
                    builder.setPositiveButton(context.getString(R.string.sure_text),
                        DialogInterface.OnClickListener { _, _ ->
                            uiState.deleteComment(context, item.commentId.toString())
                        })

                    builder.setNegativeButton(context.getString(R.string.cancel_text),
                        DialogInterface.OnClickListener { _, _ ->
                        })

                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.show()

                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                        .setTextColor(context.getColor(R.color.button_balck))
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                        .setTextColor(context.getColor(R.color.button_balck))
                    true
                }

            } else {
                binding.deleteCommentButton.visibility = View.GONE
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
        return ViewHolder(
            ItemUserCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), uiState, context)
    }
}