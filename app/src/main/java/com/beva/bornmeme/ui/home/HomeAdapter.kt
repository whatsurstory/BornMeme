package com.beva.bornmeme.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Constraints
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemHomeImgBinding
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.ui.detail.img.CommentAdapter

import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.roundToInt

class HomeAdapter(private val onClickListener: OnClickListener) : ListAdapter<Post, HomeAdapter.ViewHolder>(
    DiffCallback
) {
    //TODO: MENU drawer (navigate), chipview(onClick to change view), sortbutton(to change...)
    class ViewHolder(private val binding: ItemHomeImgBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: Post) {

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
                            Timber.d("origin width: ${width}")
                            Timber.d("origin height: ${height}")
                            Timber.d("iv width: ${binding.homeImg.width}")
                            Timber.d("iv height: ${binding.homeImg.height}")
                            Timber.d("iv x: ${(binding.homeImg.width.toFloat() / bitmap.width.toFloat())}")
                            height = ((binding.homeImg.width.toFloat() / bitmap.width.toFloat()) * bitmap.height).roundToInt()

                            Timber.d("after width: ${binding.homeImg.width}")
                            Timber.d("after height: ${height}")
                        } else if (width <= height) {
                            if (height > binding.homeImg.width * 1.5){
                                height = (binding.homeImg.width * 1.5).roundToInt()
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

            //delete for short
            binding.likeBtn.setOnClickListener{

                val builder = AlertDialog.Builder(it.context)
                builder.setMessage("Delete ${item.id}")
                builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                FirebaseFirestore.getInstance()
                    .collection("Posts")
                    .document(item.id)
                    .delete()
                    .addOnSuccessListener { Timber.d("DocumentSnapshot successfully deleted!") }
                    .addOnFailureListener { e -> Timber.w( "Error deleting document", e) }
                })
                builder.setNegativeButton("No", DialogInterface.OnClickListener{ dialog, which ->

                })
                val alertDialog: AlertDialog = builder.create()
                alertDialog.show()
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
            holder.bind(item)
        }
    }

    class OnClickListener(val clickListener: (item: Post) -> Unit) {
        fun onClick(item: Post) = clickListener(item)
    }
}