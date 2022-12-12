package com.beva.bornmeme.ui.detail.dialog

import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getColor
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.BottomsheetEditProfileBinding
import com.beva.bornmeme.loadImage
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber
import java.sql.Date


class EditProfileBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: BottomsheetEditProfileBinding
    private lateinit var viewModel: EditProfileViewModel
    lateinit var userId: String
    lateinit var uri: Uri
    private var isChange = false


    override fun getTheme() = R.style.CustomBottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            userId = bundle.getString("userId").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.let {
            val sheet = it as BottomSheetDialog
            sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding = BottomsheetEditProfileBinding.inflate(layoutInflater)

        viewModel = EditProfileViewModel(userId)

        viewModel.userData.observe(viewLifecycleOwner) {
            Timber.d("Observe user $it")
            it?.let {
                updateUserData(it)
                viewModel.blockUser = it.blockList
                viewModel.follower = it.followers
                viewModel.followUser = it.followList
            }
        }
        binding.email.isEnabled = false
        binding.emailBox.setOnClickListener {
            binding.emailBox.error = "信箱修改開發中，加速要加錢"
        }

        binding.name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.name.text?.length!! <= 20) {
                    binding.nameBox.error = null
                    binding.name.error = null
                    binding.saveButton.isEnabled = true
                    binding.saveButton.setOnClickListener {
                        saveProfileData()
                        dismiss()
                    }
                } else {
                    binding.saveButton.setOnClickListener {
                        binding.name.error = "普林ドキドキ"
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.name.text?.length!! > 20) {
                    binding.nameBox.error = "字數超過了"
                }
            }
        })

        binding.desc.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.desc.text?.length!! <= 50) {
                    binding.descBox.error = null
                    binding.desc.error = null
                    binding.saveButton.isEnabled = true
                    binding.saveButton.setOnClickListener {
                        saveProfileData()
                        dismiss()
                    }
                } else {
                    binding.saveButton.setOnClickListener {
                        binding.desc.error = "格子放不下，勸施主也放下"
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.desc.text?.length!! > 50) {
                    binding.descBox.error = "我是梗圖APP 不是Tinder"
                }
            }
        })

        binding.changePhoto.setOnClickListener {
            toAlbum()
        }

        binding.blockListBtn.setOnClickListener {
            //see the list in dialog within recycle view
            showBlockListDialog(viewModel.blockUser)
        }
        binding.followersListBtn.setOnClickListener {
            //see the list in dialog within recycle view
            showFollowersDialog(viewModel.follower)
        }
        binding.followListBtn.setOnClickListener {
            //see the list in dialog within recycle view
            showFollowListDialog(viewModel.followUser)
        }

//        binding.settingBtn.setOnClickListener {
//            findNavController()
//                .navigate(MobileNavigationDirections.navigateToFragmentSetting())
//        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserData(user: User) {
        binding.profileImg.loadImage(user.profilePhoto)

        binding.name.setText(user.userName)
        binding.email.setText(user.email)
        binding.desc.setText(user.introduce)
        binding.registerTime.text =
            "Register Time: " + Date(user.registerTime?.toDate()?.time!!).toLocaleString()
    }

    private fun toAlbum() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        requestImageLauncher.launch(intent)
    }

    private val requestImageLauncher =
        registerForActivityResult(
            ActivityResultContracts
                .StartActivityForResult()
        ) {
            //判斷在對的條件才傳值
            if (it.resultCode == RESULT_OK) {
                Timber.d("resultCode RESULT_OK")
                uri = it.data?.data as Uri
                binding.profileImg.setImageURI(uri)
                isChange = true
            } else if (it.resultCode == RESULT_CANCELED) {
                Timber.d("getImageResult cancel ${it.resultCode}")
            }
        }

    private fun saveProfileData() {
        val db = Firebase.firestore.collection("Users").document(userId)
        val originIntro = if (binding.desc.text?.trim()?.isEmpty() == true) {
            "天很藍，雲很白，不寫簡介，我就爛"
        } else {
            binding.desc.text.toString()
        }

        val originName = if (binding.name.text?.trim()?.isEmpty() == true) {
            "熬夜小心肝"
        } else {
            binding.name.text.toString()
        }

        if (isChange &&
            originIntro != UserManager.user.introduce &&
            originName != UserManager.user.userName
        ) {
            //All different
            uploadProfile2Db(uri)
            db.update("introduce", originIntro)
            db.update("userName", originName)
        } else if (!isChange &&
            (originIntro != UserManager.user.introduce ||
                    originName != UserManager.user.userName)
        ) {
            //text different
            db.update("introduce", originIntro)
            db.update("userName", originName)
        } else if (isChange &&
            originIntro == UserManager.user.introduce ||
            originName == UserManager.user.userName
        ) {
            //image different
            uploadProfile2Db(uri)
            db.update("introduce", originIntro)
            db.update("userName", originName)
        }
    }

    private fun uploadProfile2Db(data: Uri) {
        val ref = FirebaseStorage.getInstance().reference
        ref.child("profile_img/" + System.currentTimeMillis() + ".jpg")
            .putFile(data)
            .addOnSuccessListener {
                it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->

                    UserManager.user.profilePhoto = uri.toString()

                    Firebase.firestore.collection("Users")
                        .document(UserManager.user.userId!!)
                        .update("profilePhoto", uri)
                }
            }
    }

    private fun showBlockListDialog(blockUsers: List<String>) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
        builder.setTitle("封鎖名單")

        builder.setItems(blockUsers.toTypedArray()) { dialog, which ->
            //點擊後的行為
        }
        builder.setNegativeButton("離開",
            DialogInterface.OnClickListener { _, _ -> })

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()

        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setTextColor(Color.parseColor("#181A19"))

    }

    private fun showFollowersDialog(follower: List<String>) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
        builder.setTitle("誰追蹤我")

        builder.setItems(follower.toTypedArray()) { dialog, which ->
            //點擊後的行為
        }
        builder.setNegativeButton("離開",
            DialogInterface.OnClickListener { _, _ -> })

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()

        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setTextColor(Color.parseColor("#181A19"))

    }

    private fun showFollowListDialog(followUser: List<String>) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
        builder.setTitle("追蹤名單")

        builder.setItems(followUser.toTypedArray()) { dialog, which ->
            //點擊後的行為
        }
        builder.setNegativeButton("離開",
            DialogInterface.OnClickListener { _, _ -> })

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()

        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setTextColor(getColor(requireContext(),R.color.button_balck))

    }

}
