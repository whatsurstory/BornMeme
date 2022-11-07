package com.beva.bornmeme.ui.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.Constraints
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemHomeImgBinding
import com.beva.bornmeme.model.Post

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber

class HomeAdapter(private val onClickListener: OnClickListener) : ListAdapter<Post, HomeAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemHomeImgBinding) :
        RecyclerView.ViewHolder(binding.root) {

        //TODO: BUG頂部會持續新增照片、圖片高度不能用隨機高
        //用MATCH_PARENT高度不對、WRAP_CONTENT沒有畫面
        @SuppressLint("SetTextI18n")
        fun bind(item: Post) {
            //random pick a number to make the different height
            val height = (3..8).shuffled()[0] * 100

            val layoutParams = Constraints.LayoutParams(
                Constraints.LayoutParams.MATCH_PARENT,
                height
            )

//            Timber.d("weight -> ${Constraints.LayoutParams.MATCH_PARENT}")
//            Timber.d(("height -> ${Constraints.LayoutParams.WRAP_CONTENT}"))
            binding.homeImg.layoutParams = layoutParams

            Glide.with(binding.homeImg.context)
                .load(item.url)
                .centerCrop()
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                ).into(binding.homeImg)
//            Timber.d("item url -> ${item.url}")
            binding.userName.text = item.ownerId
            if (item.like.isNullOrEmpty()){
                binding.likeNum.text = "0"
            }else {
                binding.likeNum.text = item.like.size.toString()
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
        val item =getItem(position)
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