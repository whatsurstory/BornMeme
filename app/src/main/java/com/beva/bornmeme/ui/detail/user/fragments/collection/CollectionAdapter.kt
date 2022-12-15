package com.beva.bornmeme.ui.detail.user.fragments.collection

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.ItemUserCollectionBinding
import com.beva.bornmeme.loadImage
import com.beva.bornmeme.model.Folder
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.UserManager
import com.beva.bornmeme.model.UserManager.user
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

//
class CollectionAdapter(
    private val onClickListener: OnClickListener,
    val viewModel: CollectionViewModel,
    val userId: String,
    val context: Context
) : ListAdapter<Folder, CollectionAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemUserCollectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Folder,
            viewModel: CollectionViewModel,
            userId: String,
            context: Context
        ) {

            binding.collectionImg.loadImage(item.posts[0].url)
            binding.collectionFolderName.text = item.name

            if (userId == UserManager.user.userId) {
                binding.deleteFileBtn.visibility = View.VISIBLE
                binding.deleteFileBtn.setOnLongClickListener {
                    val builder: AlertDialog.Builder =
                        AlertDialog.Builder(it.context, R.style.AlertDialogTheme)
                    builder.setTitle(context.getString(R.string.remind_delete_text))
                    builder.setMessage(context.getString(R.string.check_delete_text) + item.name)
                    builder.setPositiveButton(context.getString(R.string.sure_text),
                        DialogInterface.OnClickListener { _, _ ->
                            viewModel.deleteFile(item, context)
                        })

                    builder.setNegativeButton(context.getString(R.string.cancel_text),
                        DialogInterface.OnClickListener { _, _ ->
//            Timber.d("check the selected item -> list size:${list.size} int size: ${isCheckedIndex.size}")
                        })

                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.show()

                    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                        .setTextColor(context.getColor(R.color.button_balck))
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                        .setTextColor(context.getColor(R.color.button_balck))
                    true
                }
            }
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
        return ViewHolder(
            ItemUserCollectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.itemView.setOnClickListener {
                onClickListener.onClick(item)
            }
            holder.bind(item, viewModel, userId, context)
        }
    }

    class OnClickListener(val clickListener: (item: Folder) -> Unit) {
        fun onClick(item: Folder) = clickListener(item)
    }
}