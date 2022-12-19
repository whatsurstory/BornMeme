package com.beva.bornmeme.ui.gallery

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.DialogPreviewBinding
import timber.log.Timber


class PreviewDialog : AppCompatDialogFragment() {

    private lateinit var binding: DialogPreviewBinding
    private lateinit var bitmapFromEdit: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.PreviewDialog)
        arguments?.let { bundle ->
            bitmapFromEdit = bundle.getParcelable("bitmap")!!
            Timber.d("get bitmap from edit $bitmapFromEdit")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogPreviewBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.dialog = this
        binding.previewImg.setImageBitmap(bitmapFromEdit)
        return binding.root
    }
}