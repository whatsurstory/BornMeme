package com.beva.bornmeme.ui.detail.user.fragments.favorite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemUserFavoriteBinding
import com.beva.bornmeme.databinding.ItemUserPostsBinding
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.ui.home.HomeAdapter
import com.bumptech.glide.Glide

class FavoriteAdapter(private val onClickListener: OnClickListener): ListAdapter<Post, FavoriteAdapter.ViewHolder>(DiffCallback) {


    class ViewHolder(private val binding: ItemUserFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Post){
            Glide.with(binding.favoriteImg)
                .load(item.url)
                .placeholder(R.drawable._50)
                .into(binding.favoriteImg)
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
        return ViewHolder(ItemUserFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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