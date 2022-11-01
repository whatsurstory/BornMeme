package com.beva.bornmeme.ui.slideshow


import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentCameraBinding
import kotlinx.coroutines.flow.merge

class OpenCameraFragment : Fragment() {

    private lateinit var binding: FragmentCameraBinding

    // This property is only valid between onCreateView and
    // onDestroyView.
//    private val binding get() = _binding
    private lateinit var uri: Uri
    private lateinit var bgBitmap: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            uri = bundle.getParcelable("uri")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this).get(OpenCameraViewModel::class.java)
        binding = FragmentCameraBinding.inflate(inflater, container, false)

        //transfer to bitmap
        bgBitmap = BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(uri))
        binding.originPhoto.setImageBitmap(bgBitmap)

        binding.upperText.buildDrawingCache()
        binding.originPhoto.setImageBitmap(binding.upperText.drawingCache)

        return binding.root
    }

    private fun mergeBitmap(backBitmap: Bitmap, frontBitmap: Bitmap): Bitmap {
        val bitmap = backBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        val baseRect = Rect(0, 0, backBitmap.width, backBitmap.height)
        val frontRect = Rect(0, 0, frontBitmap.width, frontBitmap.height)
        canvas.drawBitmap(frontBitmap, frontRect, baseRect, null)
        return bitmap
    }

    fun merge(view: View) {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis())
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        val uri =
            activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.apply {
            val ops = activity?.contentResolver?.openOutputStream(this)
            mergeBitmap(bgBitmap, binding.upperText.drawingCache).compress(Bitmap.CompressFormat.JPEG, 100, ops)
            ops?.close()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.originPhoto.setImageURI(uri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding
    }
}