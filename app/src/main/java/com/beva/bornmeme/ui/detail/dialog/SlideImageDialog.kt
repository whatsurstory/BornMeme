package com.beva.bornmeme.ui.detail.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Rect
import android.opengl.ETC1.getWidth
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintAttribute.setAttributes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.beva.bornmeme.databinding.DialogSlideCollectionBinding
import com.beva.bornmeme.model.Folder
import timber.log.Timber
import java.nio.Buffer

class SlideImageDialog: AppCompatDialogFragment() {

    private lateinit var binding: DialogSlideCollectionBinding
    private lateinit var viewModel: SlideViewModel
    private lateinit var folder: Folder

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let { bundle ->
            folder = bundle.getParcelable("folder")!!
            Timber.d("WelCome to FOLDERRR: arg -> $folder")
        }
        dialog?.setTransparentBackground()
        viewModel = SlideViewModel(folder)
        binding = DialogSlideCollectionBinding.inflate(layoutInflater)

        val adapter = SlideAdapter()
        binding.slideImgRecycler.adapter = adapter
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.slideImgRecycler)
        viewModel.imageItem.observe(viewLifecycleOwner, Observer {
            Timber.d("observe -> $it")
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })

        return binding.root
    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun Dialog.setTransparentBackground() {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setTitle("FOLDER_NAME")
    }
}