package com.beva.bornmeme.ui.detail.user.fragments.collection

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
    val viewModel:CollectionViewModel,
    val userId: String ): ListAdapter<Folder, CollectionAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemUserCollectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind (item: Folder,viewModel:CollectionViewModel, userId: String ) {
                    Glide.with(binding.collectionImg)
                        .load(item.posts[0].url)
                        .placeholder(R.drawable.place_holder)
                        .into(binding.collectionImg)
                binding.collectionFolderName.text = item.name

                if (userId == UserManager.user.userId) {
                    binding.deleteFileBtn.visibility = View.VISIBLE
                    binding.deleteFileBtn.setOnLongClickListener {
                        val builder: AlertDialog.Builder =
                            AlertDialog.Builder(it.context, R.style.AlertDialogTheme)
                        builder.setTitle("刪除提醒")
                        builder.setMessage("即將刪除 ${item.name}")
                        builder.setPositiveButton("確定",
                            DialogInterface.OnClickListener { _, _ ->
                            viewModel.deleteFile(item)
                        })
                        builder.setNegativeButton("離開",
                            DialogInterface.OnClickListener { _, _ ->
//            Timber.d("check the selected item -> list size:${list.size} int size: ${isCheckedIndex.size}")
                            })

                        val alertDialog: AlertDialog = builder.create()
                        alertDialog.show()

                        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                            .setTextColor(Color.parseColor("#181A19"))
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                            .setTextColor(Color.parseColor("#181A19"))
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
        return ViewHolder(ItemUserCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
        holder.itemView.setOnClickListener {
            onClickListener.onClick(item)
        }
        holder.bind(item, viewModel, userId)
    }
    }
    class OnClickListener(val clickListener: (item: Folder) -> Unit) {
        fun onClick(item: Folder) = clickListener(item)
    }
}