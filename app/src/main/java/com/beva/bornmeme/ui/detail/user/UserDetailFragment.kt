package com.beva.bornmeme.ui.detail.user

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentUserDetailBinding
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber

class UserDetailFragment : Fragment() {

    lateinit var binding : FragmentUserDetailBinding
    lateinit var viewModel:UserDetailViewModel
    lateinit var userId: String

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        arguments?.let { bundle ->
            userId = bundle.getString("userId").toString()
            Timber.d("get postId from post $userId")
        }

        viewModel = UserDetailViewModel(userId)
        val adapter = TabViewPagerAdapter(this, userId)
        binding.userViewpager.adapter = adapter

        TabLayoutMediator(binding.userTabs,binding.userViewpager) { tab,position ->
            when(position){
                0 -> {tab.text = "喜歡"}
                1 -> {tab.text = "創作"}
                2 -> {tab.text = "留言"}
                3 -> {tab.text = "收藏"}
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
            .centerCrop()
            .placeholder(R.drawable._50)
            .into(binding.userDetailImg)
        binding.userDetailName.text = user.userName

        //粉絲數
        if (user.followers.isNullOrEmpty()) {
            binding.followersText.text = "0"
        } else {
            binding.followersText.text = user.followers.size.toString()
        }
        //自我介紹
        binding.introduceText.text = user.introduce
        //被喜歡
        if (user.likeId.isNullOrEmpty()){
            binding.likesText.text = "0"
        }else {
            binding.likesText.text = user.likeId.size.toString()
        }
        //po文數
        if (user.postQuantity.isNullOrEmpty()) {
            binding.postsText.text = "0"
        } else {
            binding.postsText.text = user.postQuantity.size.toString()
        }

        //被追蹤及編輯
        if (user.userId != UserManager.user.userId) {
            binding.editIcon.visibility = View.GONE
            for (item in user.followers) {
                if (item == UserManager.user.userId) {
                    binding.add2follow.visibility = View.GONE
                    binding.alreadyFollow.visibility = View.VISIBLE
                    binding.alreadyFollow.setOnClickListener {
                        Toast.makeText(context, "已經追蹤該作者，退追請等待，加速要加錢", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    binding.alreadyFollow.visibility = View.GONE
                    binding.add2follow.visibility = View.VISIBLE
                    binding.add2follow.setOnClickListener {
                        viewModel.add2follow(user.userId.toString())
                    }
                }
            }

        } else {
            binding.editIcon.visibility = View.VISIBLE
            binding.editIcon.setOnClickListener {
                viewModel.user.value?.userId?.let { id ->
                    findNavController().navigate(MobileNavigationDirections
                        .navigateToEditProfileDialog(id))
                }
            }
        }
    }
}