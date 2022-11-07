package com.beva.bornmeme.ui.detail.user

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentUserDetailBinding
import com.google.android.material.tabs.TabLayoutMediator

class UserDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        val adapter = TabViewPagerAdapter(this)
        binding.userViewpager.adapter = adapter

        TabLayoutMediator(binding.userTabs,binding.userViewpager){tab,position ->
            when(position){
                0 -> {tab.text = "Favorite"}
                1 -> {tab.text = "Posts"}
                2 -> {tab.text = "Comment"}
                3 -> {tab.text = "Collect"}
            }
        }.attach()
        return binding.root
    }

}