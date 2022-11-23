package com.beva.bornmeme.ui.detail.dialog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.BottomsheetEditProfileBinding
import com.beva.bornmeme.model.User
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.checkerframework.checker.units.qual.s
import timber.log.Timber
import java.sql.Date


class EditProfileBottomSheet : BottomSheetDialogFragment()
{
    private lateinit var binding: BottomsheetEditProfileBinding
    private lateinit var viewModel: EditProfileViewModel
    lateinit var userId: String

    override fun getTheme() = R.style.CustomBottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.let {
            val sheet = it as BottomSheetDialog
            sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.saveButton.isClickable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomsheetEditProfileBinding.inflate(inflater,container,false)
        arguments?.let { bundle ->
            userId = bundle.getString("userId").toString()
            Timber.d("get userId from profile $userId")
        }
        viewModel = EditProfileViewModel(userId)

        viewModel.user.observe(viewLifecycleOwner) {
            Timber.d("Observe user $it")
            it?.let {
                updateUserData(it)
            }
        }

        binding.nameBox.isErrorEnabled = true
        binding.descBox.isErrorEnabled = true
        binding.email.isEnabled = false
        binding.changePhoto.setOnClickListener {
            //開啟相簿權限
            //把選到的圖片上傳到storage轉成URL，先用這個URL setImage
        }

        binding.name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.saveButton.isClickable = true
                if (binding.name.text?.length!! <= 20) {
                    binding.nameBox.error = null
                    binding.name.error = null
                    binding.saveButton.setOnClickListener {
                        Toast.makeText(context, "承蒙您的照顧了",Toast.LENGTH_SHORT).show()
                        dismiss()
                        //update data
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
                binding.saveButton.alpha = 1F
                binding.saveButton.isClickable = true
                if (binding.desc.text?.length!! <= 200) {
                    binding.descBox.error = null
                    binding.desc.error = null
                    binding.saveButton.setOnClickListener {
                        Toast.makeText(context, "承蒙您的照顧了",Toast.LENGTH_SHORT).show()
                        dismiss()
                        //TODO:update data
                    }
                } else {
                    binding.saveButton.setOnClickListener {
                        binding.desc.error = "你寫論文有這麼認真嗎?"
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.saveButton.alpha = 1F
                binding.saveButton.isClickable = true
                if (binding.desc.text.isNullOrBlank() || binding.desc.text!!.isEmpty()) {
                    binding.desc.setText("天很藍，雲很白，不寫簡介，我就爛")
                    //TODO:update to firebase user data
                } else if (binding.desc.text?.length!! > 200) {
                    binding.descBox.error = "我是梗圖APP 不是Tinder"
                }
            }
        })

        return binding.root
    }

    private fun updateUserData(user: User) {

        Glide.with(binding.profileImg)
            .load(user.profilePhoto)
            .placeholder(R.drawable.dino)
            .into(binding.profileImg)

        binding.name.setText(user.userName)
        binding.email.setText(user.email)
        binding.desc.setText(user.introduce)
        binding.registerTime.text = Date(user.registerTime?.toDate()?.time!!).toLocaleString()
//        Timber.d("binding.registerTime.text ${binding.registerTime.text}")
    //TODO: 設定大頭貼及背景顏色、追蹤誰及誰追蹤查看、封鎖名單

    }
}