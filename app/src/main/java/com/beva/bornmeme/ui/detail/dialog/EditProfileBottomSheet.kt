package com.beva.bornmeme.ui.detail.dialog

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.annotation.NonNull
import com.beva.bornmeme.MainActivity
import com.beva.bornmeme.databinding.BottomsheetEditProfileBinding
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.beva.bornmeme.model.UserManager.user
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber
import java.sql.Date


class EditProfileBottomSheet : BottomSheetDialogFragment()
{

    private lateinit var binding: BottomsheetEditProfileBinding
    private lateinit var viewModel: EditProfileViewModel
    lateinit var userId: String

    override fun getTheme() = com.beva.bornmeme.R.style.CustomBottomSheetDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.let {
            val sheet = it as BottomSheetDialog
            sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding = BottomsheetEditProfileBinding.inflate(inflater,container,false)
        arguments?.let { bundle ->
            userId = bundle.getString("userId").toString()
            Timber.d("get userId from profile $userId")
        }
        viewModel = EditProfileViewModel(userId)
        Manifest.permission()
        viewModel.user.observe(viewLifecycleOwner) {
            Timber.d("Observe user $it")
            it?.let {
                updateUserData(it)
            }
        }
        binding.saveButton.isClickable = false
        binding.email.isEnabled = false
        binding.emailBox.setOnClickListener {
            binding.emailBox.error = "信箱修改開發中，加速要加錢"
        }

        val db = Firebase.firestore.collection("Users").document(userId)
        binding.name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.saveButton.isClickable  = true
                if (binding.name.text?.length!! <= 20) {
                    binding.nameBox.error = null
                    binding.name.error = null
                    binding.saveButton.setOnClickListener {
                        val originName = binding.name.text.toString()
                        if (originName != user.userName) {
                            db.update("userName", originName)
                            dismiss()
                        }
                    }
                } else {
                    //name is lower than 1 and bigger than 20
                    binding.saveButton.setOnClickListener {
                        binding.name.error = "普林ドキドキ"
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.saveButton.isClickable = true
                if (binding.name.text.isNullOrBlank() || binding.name.text!!.isEmpty()) {
                    binding.name.setText("熬夜小心肝")
                    //update to firebase user data !!default is origin data!!
                } else if (binding.name.text?.length!! > 20) {
                    binding.nameBox.error = "字數超過了"
                }
            }
        })

        binding.desc.addTextChangedListener (object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.saveButton.isClickable = true
                if (binding.desc.text?.length!! <= 50) {
                    binding.descBox.error = null
                    binding.desc.error = null
                    binding.saveButton.setOnClickListener {
                        val originIntro = binding.desc.text.toString()
                        if (originIntro != user.introduce) {
                            db.update("introduce", originIntro)
                            dismiss()
                        }
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
                binding.saveButton.isClickable = true
                if (binding.desc.text.isNullOrBlank() || binding.desc.text!!.isEmpty()) {
                    binding.desc.setText("天很藍，雲很白，不寫簡介，我就爛")
                } else if (binding.desc.text?.length!! > 50) {
                    binding.descBox.error = "我是梗圖APP 不是Tinder"
                }
            }
        })


        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserData(user: User) {

        Glide.with(binding.profileImg)
            .load(user.profilePhoto)
            .placeholder(com.beva.bornmeme.R.drawable.dino)
            .into(binding.profileImg)

        binding.name.setText(user.userName)
        binding.email.setText(user.email)
        binding.desc.setText(user.introduce)
        binding.registerTime.text =
            "Register Time: " + Date(user.registerTime?.toDate()?.time!!).toLocaleString()
//        Timber.d("binding.registerTime.text ${binding.registerTime.text}")

        binding.changePhoto.setOnClickListener {
            toAlbum()
        }

        binding.blockListBtn.setOnClickListener {



        }
        binding.followersListBtn.setOnClickListener {

        }
        binding.followListBtn.setOnClickListener {

        }

    }

    private fun toAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            binding.profileImg.setImageURI(data?.data)
            Timber.d("setImageURI ${data?.data}")

            binding.saveButton.setOnClickListener {

                val ref = FirebaseStorage.getInstance().reference
                ref.child("profile_img/" + System.currentTimeMillis() + ".jpg")
                    .putFile(data?.data!!)
                    .addOnSuccessListener {
                        it.metadata?.reference?.downloadUrl?.addOnSuccessListener {

                            UserManager.user.profilePhoto  = it.toString()

                            Firebase.firestore.collection("Users")
                                .document(user.userId!!)
                                .update("profilePhoto", it)
                                .addOnCompleteListener {

                                    (activity as MainActivity).updateUser(user)
                                    dismiss()
                                }
                        }
                    }
            }
        }
    }


}
