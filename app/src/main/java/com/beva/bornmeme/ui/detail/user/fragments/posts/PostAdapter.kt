package com.beva.bornmeme.ui.detail.user.fragments.posts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemHomeImgBinding
import com.beva.bornmeme.databinding.ItemUserPostsBinding
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.User
import com.beva.bornmeme.ui.home.HomeAdapter
import com.bumptech.glide.Glide
import kotlinx.coroutines.NonDisposableHandle.parent

class PostAdapter : ListAdapter<Post, PostAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemUserPostsBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(item: Post){
                Glide.with(binding.postsImg.context)
                    .load(item.url)
                    .placeholder(R.drawable._50)
                    .into(binding.postsImg)
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
        return ViewHolder(ItemUserPostsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}