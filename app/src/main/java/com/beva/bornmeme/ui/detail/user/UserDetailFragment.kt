package com.beva.bornmeme.ui.detail.user

import android.annotation.SuppressLint
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
import com.beva.bornmeme.loadImage
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.beva.bornmeme.model.UserManager.user
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber

class UserDetailFragment : Fragment() {

    lateinit var binding: FragmentUserDetailBinding
    lateinit var viewModel: UserDetailViewModel
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

        viewModel = UserDetailViewModel(userId,requireContext())
        val adapter = TabViewPagerAdapter(this, userId)
        binding.userViewpager.adapter = adapter

        TabLayoutMediator(binding.userTabs, binding.userViewpager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Likes"
                }
                1 -> {
                    tab.text = "Posts"
                }
                2 -> {
                    tab.text = "Text"
                }
                3 -> {
                    tab.text = "Files"
                }
            }
        }.attach()

        viewModel.userData.observe(viewLifecycleOwner) {
            Timber.d("Observe user $it")
            it?.let {
                updateUi(it)
            }
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun updateUi(user: User) {

        binding.userDetailImg.loadImage(user.profilePhoto)

        binding.userDetailName.text = user.userName

        binding.followersText.text =
            if (user.followers.isNullOrEmpty()) "0" else user.followers.size.toString()

        binding.introduceText.text = user.introduce

        binding.likesText.text =
            if (user.likeId.isNullOrEmpty()) "0" else user.likeId.size.toString()

        binding.postsText.text =
            if (user.postQuantity.isNullOrEmpty()) "0" else user.postQuantity.size.toString()

        //被追蹤及編輯
        if (user.userId != UserManager.user.userId) {
            binding.editIcon.visibility = View.GONE
            for (item in user.followers) {
                if (item == UserManager.user.userId) {
                    binding.add2follow.visibility = View.GONE
                    binding.alreadyFollow.visibility = View.VISIBLE
                    binding.alreadyFollow.setOnClickListener {
                        viewModel.cancel2Follow(user.userId.toString(),requireContext())
                    }
                } else {
                    binding.alreadyFollow.visibility = View.GONE
                    binding.add2follow.visibility = View.VISIBLE
                    binding.add2follow.setOnClickListener {
                        viewModel.add2follow(user.userId.toString(),requireContext())
                    }
                }
            }

        } else {
            binding.editIcon.visibility = View.VISIBLE
            binding.editIcon.setOnClickListener {
                viewModel.userData.value?.userId?.let { id ->
                    findNavController().navigate(
                        MobileNavigationDirections
                            .navigateToEditProfileDialog(id)
                    )
                }
            }
        }
    }
}