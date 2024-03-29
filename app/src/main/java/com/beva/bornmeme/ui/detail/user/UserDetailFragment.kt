package com.beva.bornmeme.ui.detail.user

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MainApplication
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentUserDetailBinding
import com.beva.bornmeme.databinding.SnackBarCustomBinding
import com.beva.bornmeme.loadImage
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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
//            Timber.d("get postId from post $userId")
        }

        viewModel = UserDetailViewModel(userId, MainApplication.instance)
        val adapter = TabViewPagerAdapter(this, userId)
        binding.userViewpager.adapter = adapter

        TabLayoutMediator(binding.userTabs, binding.userViewpager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = requireContext().getString(R.string.like_text)
                }
                1 -> {
                    tab.text = requireContext().getString(R.string.post_text)
                }
                2 -> {
                    tab.text = requireContext().getString(R.string.text_text)
                }
                3 -> {
                    tab.text = requireContext().getString(R.string.file_text)
                }
            }
        }.attach()

        viewModel.userData.observe(viewLifecycleOwner) { user ->
//            Timber.d("Observe user $user")
            user?.let {
                updateUi(it)
            }
            //the menu button to report and other feature
            val popupMenu = PopupMenu(
                context,
                binding.userReportBtn
            )
            if (user.userId != UserManager.user.userId) {

                popupMenu.menu.add(Menu.NONE, 0, 0, getString(R.string.report_text))
                popupMenu.menu.add(Menu.NONE, 1, 1, getString(R.string.block_text))
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        0 -> reportDialog(user.userId.toString())
                        1 -> add2Block(user.userId.toString())
                    }
                    false
                }

            } else {
                popupMenu.menu.add(Menu.NONE, 0, 0, getString(R.string.setting_text))
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        0 -> findNavController()
                            .navigate(MobileNavigationDirections.navigateToFragmentSetting())
                    }
                    false
                }
            }
            binding.userReportBtn.setOnClickListener {
                popupMenu.show()
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
                        viewModel.cancel2Follow(user.userId.toString(), MainApplication.instance)
                    }
                } else {
                    binding.alreadyFollow.visibility = View.GONE
                    binding.add2follow.visibility = View.VISIBLE
                    binding.add2follow.setOnClickListener {
                        viewModel.add2follow(user.userId.toString(), MainApplication.instance)
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

    @SuppressLint("ResourceAsColor")
    private fun reportDialog(userId: String) {
        val data = arrayOf("色情", "暴力", "賭博", "非法交易", "種族歧視")

        val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
        builder.setTitle(getString(R.string.choose_report_text))
        builder.setMultiChoiceItems(data, null) { dialog, i, b ->
            val currentItem = data[i]
        }
        builder.setPositiveButton(getString(R.string.sure_text)) { dialogInterface, j ->
//            for (i in data.indices) if (selected[i]) {
//                selected[i] = false
//            }
            val customSnack = Snackbar.make(requireView(), "", Snackbar.LENGTH_INDEFINITE)
            val layout = customSnack.view as Snackbar.SnackbarLayout
            val bind = SnackBarCustomBinding.inflate(layoutInflater)
            bind.notToBlockBtn.setOnClickListener {
                customSnack.dismiss()
            }
            bind.toBlockBtn.setOnClickListener {
                UserManager.user.blockList += userId
                UserManager.user.userId?.let { id ->
                    Firebase.firestore.collection(getString(R.string.user_collection_text))
                        .document(id)
                        .update("blockList", UserManager.user.blockList)
                        .addOnCompleteListener {
                            customSnack.dismiss()
                            findNavController().navigateUp()
                        }
                }
            }
            layout.addView(bind.root, 0)
            customSnack.setBackgroundTint(
                getColor(
                    requireContext(), R.color.white
                )
            )
            customSnack.view.layoutParams =
                (customSnack.view.layoutParams as FrameLayout.LayoutParams)
                    .apply {
                        gravity = Gravity.TOP
                    }
            customSnack.show()
        }
        builder.setNegativeButton(getString(R.string.cancel_text)) { dialog, i ->
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            .setTextColor(getColor(requireContext(), R.color.button_balck))
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setTextColor(getColor(requireContext(), R.color.button_balck))
    }

    @SuppressLint("SetTextI18n")
    private fun add2Block(userId: String) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_custom_delete, null)
        builder.setView(view)

        val alertDialog: AlertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        val message = view.findViewById<TextView>(R.id.delete_message)
        message.text = getString(R.string.block_sure_text)

        val okay = view.findViewById<Button>(R.id.okay_delete_btn)
        okay.setOnClickListener {
            UserManager.user.blockList += userId
            UserManager.user.userId?.let { id ->
                Firebase.firestore.collection(getString(R.string.user_collection_text))
                    .document(id)
                    .update("blockList", UserManager.user.blockList)
                    .addOnCompleteListener {
                        alertDialog.dismiss()
                        findNavController().navigateUp()
                    }
            }
        }
        val cancel = view.findViewById<Button>(R.id.cancel_button)
        cancel.setOnClickListener { alertDialog.dismiss() }
    }
}