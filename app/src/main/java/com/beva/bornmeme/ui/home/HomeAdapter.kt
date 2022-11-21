package com.beva.bornmeme.ui.home

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.Constraints
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemHomeImgBinding
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.User

import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import timber.log.Timber
import kotlin.math.roundToInt

class HomeAdapter(private val onClickListener: OnClickListener,private val uiState: HomeViewModel.UiState) : ListAdapter<Post, HomeAdapter.ViewHolder>(
    DiffCallback
) {
    class ViewHolder(private val binding: ItemHomeImgBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: Post, uiState: HomeViewModel.UiState) {

            Glide.with(binding.homeImg.context)
                .load(item.url).centerCrop()
                .into(object : SimpleTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        val bitmap = (resource as BitmapDrawable).bitmap
                        Timber.d("check image in glide 寬${bitmap.width}, 高${bitmap.height}")
                        binding.homeImg.setImageBitmap(bitmap)
                        val width = bitmap.width
                        var height = bitmap.height
                        if (width > height) {
                            Timber.d("寬大於高")
                            Timber.d("origin width: $width")
                            Timber.d("origin height: $height")
                            Timber.d("iv width: ${binding.homeImg.width}")
                            Timber.d("iv height: ${binding.homeImg.height}")
                            Timber.d("iv x: ${(binding.homeImg.width.toFloat() / bitmap.width.toFloat())}")
                            height = ((binding.homeImg.width.toFloat() / bitmap.width.toFloat()) * bitmap.height).roundToInt()

                            Timber.d("after width: ${binding.homeImg.width}")
                            Timber.d("after height: $height")
                        } else if (width <= height) {
                            if (height > binding.homeImg.width * 1.3){
                                height = (binding.homeImg.width * 1.3).roundToInt()
                            }
                            Timber.d("寬小於高 $height")
                        }

                        val layoutParams = Constraints.LayoutParams(
                            Constraints.LayoutParams.MATCH_PARENT,
                            height
                        )
                        Timber.d("check image in params 寬$width 高$height")
                        binding.homeImg.layoutParams = layoutParams
                        binding.homeImg.scaleType = ImageView.ScaleType.CENTER_CROP
                        Timber.d("image 寬${binding.homeImg.width} 高 ${binding.homeImg.height}")

                    }
                })

            binding.userName.text = item.title
            if (item.like.isNullOrEmpty()) {
                binding.likeNum.text = "0"
            } else {
                binding.likeNum.text = item.like.size.toString()
            }

            uiState.getUserImg(item.ownerId) { user: User ->

                Timber.d("img => ${user.profilePhoto}")
                Glide.with(binding.userImg)
                    .load(user.profilePhoto)
                    .placeholder(R.drawable._50)
                    .into(binding.userImg)
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
        val binding = ItemHomeImgBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item =getItem(position)
        item?.let {
            holder.itemView.setOnClickListener {
                onClickListener.onClick(item)
            }
            holder.bind(item, uiState)
        }
    }

    class OnClickListener(val clickListener: (item: Post) -> Unit) {
        fun onClick(item: Post) = clickListener(item)
    }
}