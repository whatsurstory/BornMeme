package com.beva.bornmeme.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentHomeBinding
import com.beva.bornmeme.databinding.ItemTagBinding
import com.beva.bornmeme.model.Post
import timber.log.Timber


//
class TagAdapter(private val onClickListener: OnClickListener): ListAdapter<String, TagAdapter.ViewHolder>(DiffCallback) {

    private var selectItem = -1

    class ViewHolder (private val binding: ItemTagBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(item: String, selectedItem:Int){
            binding.chipText.text = item
            if (adapterPosition == selectedItem) {
                //recycler reuse if else condition
                Timber.d("adapterPosition $adapterPosition")
                binding.chipText.alpha = 0.4F
            } else {
                binding.chipText.alpha = 1F
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return (oldItem == newItem)
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @SuppressLint("ResourceAsColor", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = getItem(position)
        item?.let {
            holder.itemView.setOnClickListener {
                onClickListener.onClick(item)
                selectItem = position
                Timber.d("selectItem $selectItem position $position")
                notifyDataSetChanged()
            }
            selectItem != -1
            holder.bind(item, selectItem)
        }
    }

    class OnClickListener(val clickListener: (item: String) -> Unit) {
        fun onClick(item: String) = clickListener(item)
    }
}