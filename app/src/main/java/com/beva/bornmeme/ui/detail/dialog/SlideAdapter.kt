package com.beva.bornmeme.ui.detail.dialog

import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemSlideImageBinding
import com.beva.bornmeme.loadImage
import com.beva.bornmeme.model.Folder
import com.beva.bornmeme.model.FolderData
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.ui.detail.user.fragments.posts.PostAdapter
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream

class SlideAdapter(private val onClickListener: OnClickListener
): ListAdapter<FolderData, SlideAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback: DiffUtil.ItemCallback<FolderData>(){
        override fun areItemsTheSame(oldItem: FolderData, newItem: FolderData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: FolderData, newItem: FolderData): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(private val binding: ItemSlideImageBinding) :
        RecyclerView.ViewHolder(binding.root){
        fun bind (item: FolderData) {
            binding.slideImage.loadImage(item.url)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSlideImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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

    class OnClickListener(val clickListener: (item: FolderData) -> Unit) {
        fun onClick(item: FolderData) = clickListener(item)
    }

}