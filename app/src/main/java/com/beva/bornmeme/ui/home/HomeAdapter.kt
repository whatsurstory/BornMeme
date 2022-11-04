package com.beva.bornmeme.ui.home

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Constraints
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.loadAny
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemHomeImgBinding
import com.beva.bornmeme.model.Post

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.common.base.Strings.isNullOrEmpty

class HomeAdapter : ListAdapter<Post, HomeAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemHomeImgBinding) :
        RecyclerView.ViewHolder(binding.root) {

        //TODO: BUG頂部會持續新增照片
        @SuppressLint("SetTextI18n")
        fun bind(item: Post) {
            //random pick a number to make the different height
            val height = (3..8).shuffled()[0] * 100
            Log.d("Bevaaaaa", "position=$layoutPosition, height=$height")

            val layoutParams = Constraints.LayoutParams(
                Constraints.LayoutParams.MATCH_PARENT,
                height
            )
            binding.homeImg.layoutParams = layoutParams

            Glide.with(binding.homeImg.context)
                .load(item.url)
                .centerCrop()
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                ).into(binding.homeImg)
            binding.userName.text = item.ownerId
            if (item.like.isNullOrEmpty()){
                binding.likeNum.text = "0"
            }else {
                binding.likeNum.text = item.like.size.toString()
            //databinding
            //binding.executePendingBindings()
            }
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
            ItemHomeImgBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}