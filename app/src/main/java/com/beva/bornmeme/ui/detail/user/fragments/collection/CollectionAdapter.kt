package com.beva.bornmeme.ui.detail.user.fragments.collection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemUserCollectionBinding
import com.beva.bornmeme.model.Folder
import com.beva.bornmeme.model.Post
import com.bumptech.glide.Glide

//
class CollectionAdapter(private val onClickListener: OnClickListener): ListAdapter<Folder, CollectionAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemUserCollectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind (item: Folder) {
                    Glide.with(binding.collectionImg)
                        .load(item.posts[0].url)
                        .placeholder(R.drawable._50)
                        .into(binding.collectionImg)
                binding.collectionFolderName.text = item.name
            }

    }
    companion object DiffCallback : DiffUtil.ItemCallback<Folder>() {
        override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return (oldItem == newItem)
        }

        override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemUserCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
    class OnClickListener(val clickListener: (item: Folder) -> Unit) {
        fun onClick(item: Folder) = clickListener(item)
    }
}