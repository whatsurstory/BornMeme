package com.beva.bornmeme.ui.home

import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.databinding.FragmentHomeBinding
import com.beva.bornmeme.databinding.ItemHomeImgBinding
import com.beva.bornmeme.model.ImgResource
import com.beva.bornmeme.model.PhotoInformation

class HomeAdapter: ListAdapter<ImgResource, HomeAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemHomeImgBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind (item: ImgResource){
                binding.userName.text = item.text
                binding.userImg.setImageBitmap(item.url)
            }
        }

    companion object DiffCallback : DiffUtil.ItemCallback<ImgResource>() {
        override fun areItemsTheSame(oldItem: ImgResource, newItem: ImgResource): Boolean {
            return (oldItem === newItem)
        }

        override fun areContentsTheSame(oldItem: ImgResource, newItem: ImgResource): Boolean {
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