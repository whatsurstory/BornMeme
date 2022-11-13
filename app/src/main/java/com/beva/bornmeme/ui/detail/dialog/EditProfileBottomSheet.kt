package com.beva.bornmeme.ui.detail.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.beva.bornmeme.databinding.BottomsheetEditProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EditProfileBottomSheet : BottomSheetDialogFragment()
{
    private lateinit var binding: BottomsheetEditProfileBinding
    private lateinit var viewModel: EditProfileViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        viewModel = ViewModelProvider(activity).get(EditProfileViewModel::class.java)
        binding.saveButton.setOnClickListener {
            saveAction()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomsheetEditProfileBinding.inflate(inflater,container,false)
        return binding.root
    }


    private fun saveAction()
    {
        viewModel.name.value = binding.name.text.toString()
        viewModel.email.value = binding.email.text.toString()
        viewModel.desc.value = binding.desc.text.toString()
        binding.name.setText("")
        binding.email.setText("")
        binding.desc.setText("")
        dismiss()
    }

}