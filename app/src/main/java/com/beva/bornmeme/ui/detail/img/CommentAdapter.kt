package com.beva.bornmeme.ui.detail.img

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemDetailCommentChildBinding
import com.beva.bornmeme.databinding.ItemDetailCommentParentBinding
import com.beva.bornmeme.databinding.SnackBarCustomBinding
import com.beva.bornmeme.loadImage
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class CommentAdapter(
    private val uiState: ImgDetailViewModel.UiState,
    val viewModel: ImgDetailViewModel,
    val fragment: ImgDetailFragment,
    val inflater: LayoutInflater,
    val context: Context
) : ListAdapter<CommentCell, RecyclerView.ViewHolder>(DiffCallback) {

    class ParentViewHolder(private var binding: ItemDetailCommentParentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(
            item: CommentCell.ParentComment,
            uiState: ImgDetailViewModel.UiState,
            viewModel: ImgDetailViewModel,
            fragment: ImgDetailFragment,
            context: Context,
            inflater: LayoutInflater
        ) {

            if (item.hasChild) {
                binding.seeMoreBtn.visibility = View.VISIBLE
                binding.backBtn.visibility = View.GONE
            } else {
                binding.seeMoreBtn.visibility = View.GONE
            }
            binding.seeMoreBtn.setOnClickListener {
                uiState.onClickToSeeMore(item)
                binding.backBtn.visibility = View.VISIBLE
            }

            binding.backBtn.setOnClickListener {
                uiState.onClickToBack(item.id)
                item.hasChild = true
            }

            binding.replyBtn.setOnClickListener {
                uiState.onClickToReply(item)
//                Timber.d("Observe replybtn ${item.comment.commentId}")
            }
            binding.commentLikeBtn.setOnClickListener {
                item.comment.commentId?.let { id ->
                    uiState.onClickToLike(id, context)
                }
            }
            binding.commentDislikeBtn.setOnClickListener {
                item.comment.commentId?.let { id ->
                    uiState.onClickToDislike(id, context)
                }
            }

            binding.commentLikeNum.text =
                if (item.comment.like.isNullOrEmpty()) "0" else item.comment.like.size.toString()

            binding.commentDislikeNum.text =
                if (item.comment.dislike.isNullOrEmpty()) "0" else item.comment.dislike.size.toString()

            binding.commentZoneText.text = item.comment.content
//            val timeString = item.comment.time?.toDate()?.toString()
            val commentTime = item.comment.time?.toDate()?.time
            val currentTime = System.currentTimeMillis()
            Timber.d("currentTime $currentTime")
            val seconds = (currentTime - commentTime!!) / 1000
            val minutes = seconds / 60
            val hour = minutes / 60
            val day = hour / 24
            if (seconds <= 50) {
                binding.commentTime.text = "$seconds seconds ago"
            } else if (seconds in 60..3600) {
                binding.commentTime.text = "$minutes minutes ago"
            } else if (seconds in 3600..86400) {
                binding.commentTime.text = "$hour hour ago"
            } else if (seconds >= 86400) {
                binding.commentTime.text = "$day days ago"
            }
//            Timber.d("秒 $seconds 分 $minutes 時 $hour 天 $day")

            item.comment.userId.let {
                uiState.getUserImg(context, it) { user: User ->
//                    Timber.d("img => ${user.profilePhoto}")
                    binding.commentUserImg.loadImage(user.profilePhoto)
                    binding.commentUserImg.setOnClickListener {
                        viewModel.navigate2UserDetail(fragment, item.comment.userId)
                    }
                    binding.commentUserName.text = user.userName
                }
            }
            if (item.comment.userId != UserManager.user.userId) {
                binding.commentParentReportBtn.visibility = View.VISIBLE
                binding.commentParentReportBtn.setOnClickListener {
                    reportCommentDialog(item.comment.userId, context, fragment, inflater)
                }
            } else {
                binding.commentParentReportBtn.visibility = View.INVISIBLE
            }
        }
        private fun reportCommentDialog(id: String, context: Context, fragment: ImgDetailFragment, inflater: LayoutInflater) {
            @SuppressLint("ResourceAsColor")
            val data = arrayOf("色情", "暴力", "賭博", "非法交易", "種族歧視")

            val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
            builder.setTitle(context.getString(R.string.report_reason_text))
            builder.setMultiChoiceItems(data, null) { dialog, i, b ->
                val currentItem = data[i]
            }
            builder.setPositiveButton(context.getString(R.string.sure_text)) { dialogInterface, j ->
//            for (i in data.indices) if (selected[i]) {
//                selected[i] = false
//            }
                val customSnack = fragment.view?.let {
                    Snackbar.make(it, "", Snackbar.LENGTH_INDEFINITE)
                }
                val layout = customSnack?.view as Snackbar.SnackbarLayout
                val bind = SnackBarCustomBinding.inflate(inflater)
                bind.notToBlockBtn.setOnClickListener {
                    customSnack.dismiss()
                }
                bind.toBlockBtn.setOnClickListener {
                    UserManager.user.blockList += id
                    UserManager.user.userId?.let { id ->
                        Firebase.firestore.collection(context.getString(R.string.user_collection_text))
                            .document(id)
                            .update("blockList", UserManager.user.blockList)
                            .addOnCompleteListener {
                                customSnack.dismiss()
                                NavHostFragment.findNavController(fragment).navigateUp()
                            }
                    }
                }
                layout.addView(bind.root, 0)
                customSnack.setBackgroundTint(
                    context.getColor(R.color.white)
                )
                customSnack.view.layoutParams =
                    (customSnack.view.layoutParams as FrameLayout.LayoutParams)
                        .apply {
                            gravity = Gravity.TOP
                        }
                customSnack.show()
            }
            builder.setNegativeButton(context.getString(R.string.cancel_text)) { dialog, i ->
            }
            val dialog = builder.create()
            dialog.show()
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(context.getColor(R.color.button_balck))
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(context.getColor(R.color.button_balck))
        }

    }

    class ChildViewHolder(private var binding: ItemDetailCommentChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(
            item: CommentCell.ChildComment,
            uiState: ImgDetailViewModel.UiState,
            viewModel: ImgDetailViewModel,
            fragment: ImgDetailFragment,
            context: Context,
            inflater: LayoutInflater
        ) {

            Timber.d("ChildViewHolder $adapterPosition")
            binding.childDislikeBtn.setOnClickListener {
                item.comment.commentId?.let { id ->
                    uiState.onClickToDislike(id, context)
                }
            }
            binding.childLikeBtn.setOnClickListener {
                item.comment.commentId?.let { id ->
                    uiState.onClickToLike(id, context)
                }
            }
            binding.childLikeNum.text =
                if (item.comment.like.isNullOrEmpty()) "0" else item.comment.like.size.toString()

            binding.childDislikeNum.text =
                if (item.comment.dislike.isNullOrEmpty()) "0" else item.comment.dislike.size.toString()

            binding.childZoneText.text = item.comment.content
//            val timeString = item.comment.time?.toDate()?.toString()
            val commentTime = item.comment.time?.toDate()?.time
            val currentTime = System.currentTimeMillis()
            val seconds = (currentTime - commentTime!!) / 1000
            val minutes = seconds / 60
            val hour = minutes / 60
            val day = hour / 24
            if (seconds <= 50) {
                binding.childTime.text = "$seconds seconds ago"
            } else if (seconds in 60..3600) {
                binding.childTime.text = "$minutes minutes ago"
            } else if (seconds in 3600..86400) {
                binding.childTime.text = "$hour hour ago"
            } else if (seconds >= 86400) {
                binding.childTime.text = "$day days ago"
            }
//            Timber.d("秒 $seconds 分 $minutes 時 $hour 天 $day")

            item.comment.userId.let {
                uiState.getUserImg(context, it) { user: User ->
//                    Timber.d("img => ${user.profilePhoto}")
                    binding.childUserImg.loadImage(user.profilePhoto)
                    binding.childUserImg.setOnClickListener {
                        viewModel.navigate2UserDetail(fragment, item.comment.userId)
                    }
                    binding.childUserName.text = user.userName
                }
            }
            if (item.comment.userId != UserManager.user.userId) {
                binding.commentChildReportBtn.visibility = View.VISIBLE
                binding.commentChildReportBtn.setOnClickListener {
                    reportCommentDialog(item.comment.userId, context, fragment, inflater)
                }
            } else {
                binding.commentChildReportBtn.visibility = View.INVISIBLE
            }
        }
        private fun reportCommentDialog(id: String, context: Context, fragment: ImgDetailFragment, inflater: LayoutInflater) {
            @SuppressLint("ResourceAsColor")
            val data = arrayOf("色情", "暴力", "賭博", "非法交易", "種族歧視")

            val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
            builder.setTitle(context.getString(R.string.report_reason_text))
            builder.setMultiChoiceItems(data, null) { dialog, i, b ->
                val currentItem = data[i]
            }
            builder.setPositiveButton(context.getString(R.string.sure_text)) { dialogInterface, j ->
//            for (i in data.indices) if (selected[i]) {
//                selected[i] = false
//            }
                val customSnack = fragment.view?.let {
                    Snackbar.make(it, "", Snackbar.LENGTH_INDEFINITE)
                }
                val layout = customSnack?.view as Snackbar.SnackbarLayout
                val bind = SnackBarCustomBinding.inflate(inflater)
                bind.notToBlockBtn.setOnClickListener {
                    customSnack.dismiss()
                }
                bind.toBlockBtn.setOnClickListener {
                    UserManager.user.blockList += id
                    UserManager.user.userId?.let { id ->
                        Firebase.firestore.collection(context.getString(R.string.user_collection_text))
                            .document(id)
                            .update("blockList", UserManager.user.blockList)
                            .addOnCompleteListener {
                                customSnack.dismiss()
                                NavHostFragment.findNavController(fragment).navigateUp()
                            }
                    }
                }
                layout.addView(bind.root, 0)
                customSnack.setBackgroundTint(
                    context.getColor(R.color.white)
                )
                customSnack.view.layoutParams =
                    (customSnack.view.layoutParams as FrameLayout.LayoutParams)
                        .apply {
                            gravity = Gravity.TOP
                        }
                customSnack.show()
            }
            builder.setNegativeButton(context.getString(R.string.cancel_text)) { dialog, i ->
            }
            val dialog = builder.create()
            dialog.show()
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(context.getColor(R.color.button_balck))
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(context.getColor(R.color.button_balck))
        }
    }


    companion object DiffCallback : DiffUtil.ItemCallback<CommentCell>() {
        override fun areItemsTheSame(oldItem: CommentCell, newItem: CommentCell): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: CommentCell, newItem: CommentCell): Boolean {
            return oldItem == newItem
        }

        private const val ITEM_VIEW_TYPE_PARENT = 0x00
        private const val ITEM_VIEW_TYPE_CHILD = 0x01
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_PARENT -> CommentAdapter.ParentViewHolder(
                ItemDetailCommentParentBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            ITEM_VIEW_TYPE_CHILD -> CommentAdapter.ChildViewHolder(
                ItemDetailCommentChildBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is ParentViewHolder -> {
                val data = getItem(position) as CommentCell.ParentComment
                holder.bind(data, uiState, viewModel, fragment, context, inflater)
//                Timber.d("data $data position $position")
            }
            is ChildViewHolder -> {
                val data = getItem(position) as CommentCell.ChildComment
                holder.bind(data, uiState, viewModel, fragment, context, inflater)
//                Timber.d("data $data position $position")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CommentCell.ParentComment -> ITEM_VIEW_TYPE_PARENT
            is CommentCell.ChildComment -> ITEM_VIEW_TYPE_CHILD
        }
    }
}