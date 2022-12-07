package com.beva.bornmeme.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.helper.widget.Carousel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemVerticalImageListBinding
import com.beva.bornmeme.loadImage
import com.beva.bornmeme.model.Image
import com.bumptech.glide.Glide

class GalleryAdapter(
    private val onClickListener: OnClickListener
) : ListAdapter<Image, GalleryAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemVerticalImageListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Image) {
            binding.verticalListImage.loadImage(item.url)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Image>() {
        override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemVerticalImageListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
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

    class OnClickListener(val clickListener: (item: Image) -> Unit) {
        fun onClick(item: Image) = clickListener(item)
    }
}