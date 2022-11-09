package com.beva.bornmeme.ui.detail.user

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentUserDetailBinding
import com.beva.bornmeme.model.User
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber

class UserDetailFragment : Fragment() {

    lateinit var binding : FragmentUserDetailBinding
    lateinit var viewModel:UserDetailViewModel
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        viewModel = UserDetailViewModel()
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

        viewModel.user.observe(viewLifecycleOwner) {
            Timber.d("Observe user $it")
            it?.let {
                updateUi(it)
            }
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun updateUi(user: User) {
        Glide.with(binding.userDetailImg)
            .load(user.profilePhoto)
            .placeholder(R.drawable._50)
            .into(binding.userDetailImg)
        binding.userDetailName.text = user.userName
        binding.followersText.text = "${user.followers.size} followers"
        binding.introduceText.text = user.introduce
    }
}