package com.beva.bornmeme.ui.detail.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.beva.bornmeme.databinding.DialogSlideCollectionBinding

class SlideImageDialog: AppCompatDialogFragment() {

    private lateinit var binding: DialogSlideCollectionBinding
    private lateinit var viewModel: SlideViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogSlideCollectionBinding.inflate(layoutInflater)

        val adapter = SlideAdapter()
        binding.slideImgRecycler.adapter = adapter


        return binding.root
    }

}