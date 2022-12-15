package com.beva.bornmeme.ui.detail.user.fragments.posts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemHomeImgBinding
import com.beva.bornmeme.databinding.ItemUserPostsBinding
import com.beva.bornmeme.loadImage
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.User
import com.beva.bornmeme.ui.detail.user.fragments.favorite.FavoriteAdapter
import com.beva.bornmeme.ui.home.HomeAdapter
import com.bumptech.glide.Glide
import kotlinx.coroutines.NonDisposableHandle.parent

class PostAdapter(private val onClickListener: OnClickListener) :
    ListAdapter<Post, PostAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemUserPostsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Post) {
            binding.postsImg.loadImage(item.url)
        }
    }


    companion object DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return (oldItem == newItem)
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemUserPostsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.itemView.setOnClickListener {
                onClickListener.onClick(item)
            }
            holder.bind(item)
        }

    }

    class OnClickListener(val clickListener: (item: Post) -> Unit) {
        fun onClick(item: Post) = clickListener(item)
    }
}