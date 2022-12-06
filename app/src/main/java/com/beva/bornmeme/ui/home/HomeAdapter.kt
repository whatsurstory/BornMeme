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

            //setting the height and weight let ui draw the image in begin
            val width = item.imageWidth
            var height = item.imageHeight
            val screenWidth = binding.root.context.resources.displayMetrics.widthPixels
            Timber.w("=====================================================")
            Timber.i("image position=${adapterPosition}")
            Timber.d("screenWidth $screenWidth")
            val deviceDensity = binding.root.context.resources.displayMetrics.density
            Timber.d("density $deviceDensity")
            Timber.d("density ${binding.root.context.resources.displayMetrics.densityDpi}")

            //12 * 3 (margin)
            val totalLayoutPadding = 36 * deviceDensity
            val itemWidth = (screenWidth - totalLayoutPadding) / 2
            Timber.d("itemWidth $itemWidth")


            if (width > height) {
                Timber.d("width > height")
                Timber.d("origin width: $width")
                Timber.d("origin height: $height")

                height = ((itemWidth / item.imageWidth.toFloat()) * item.imageHeight).roundToInt()

                Timber.d("after width: $itemWidth")
                Timber.d("after height: $height")

            } else if (width <= height) {
                Timber.d("width <= height")
                Timber.d("origin width: $width")
                Timber.d("origin height: $height")

                if (height > itemWidth * 1.3){
                    height = (itemWidth * 1.3).roundToInt()
                }
                Timber.d("after width: $itemWidth")
                Timber.d("after height: $height")
            }

            val layoutParams = Constraints.LayoutParams(
                Constraints.LayoutParams.MATCH_PARENT,
                height
            )

            Timber.d("check image in params 寬$width 高$height")
            binding.homeImg.layoutParams = layoutParams
            binding.homeImg.scaleType = ImageView.ScaleType.CENTER_CROP

            Glide.with(binding.homeImg.context)
                .load(item.url).centerCrop()
                .into(binding.homeImg)

            Timber.d("image final width->${binding.homeImg.width} final height->${binding.homeImg.height}")
            Timber.w("=====================================================")

            binding.userName.text = item.title
            if (item.like.isNullOrEmpty()) {
                binding.likeNum.text = "0"
            } else {
                binding.likeNum.text = item.like.size.toString()
            }

            uiState.getUserImg(item.ownerId) { user: User ->
                Glide.with(binding.userImg)
                    .load(user.profilePhoto)
                    .placeholder(R.drawable.place_holder)
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