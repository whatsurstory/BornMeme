package com.beva.bornmeme.ui.slideshow


import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.beva.bornmeme.databinding.FragmentCameraBinding


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
        //when you try to build the cache in the onCreate method,
        // the drawing hasn't happened yet so the drawingCache should have nothing.
        // Either put the buildDrawingChache method in the onClick method.
        // Or use the following code in onCreate.
        //val vto: ViewTreeObserver = editText.getViewTreeObserver()
        //vto.addOnGlobalLayoutListener { editText.buildDrawingCache() }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this).get(OpenCameraViewModel::class.java)
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        val photo = binding.originPhoto
        val upperText = binding.upperText
        val bottomText = binding.bottomText

        binding.previewBtn.setOnClickListener {
            //transfer to bitmap
            upperText.toString()
            bottomText.toString()
            upperText.buildDrawingCache()
            bottomText.buildDrawingCache()
            bgBitmap = BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(uri))
            photo.setImageBitmap(bgBitmap)
            photo.setImageBitmap(binding.upperText.drawingCache)
            photo.setImageBitmap(binding.bottomText.drawingCache)

            merge(it)
        }

        return binding.root
    }
//BitmapFactory.decodeResource returns a Bitmap, Bitmap.copy creates a mutable copy of the image using the option specified.
//BitMap.Config.ARGB_8888: Each pixel is stored on 4 bytes.

//    private fun mergeBitmap(
//        backBitmap: Bitmap,
//        upTextBitmap: Bitmap
//    ): Bitmap {
//        val bitmap = backBitmap.copy(Bitmap.Config.ARGB_8888, true)
//        val canvas = Canvas(bitmap)
//        val baseRect = Rect(0, 0, backBitmap.width, backBitmap.height)
//        val upRect = Rect(10, 10, upTextBitmap.width, upTextBitmap.height)
//        canvas.drawBitmap(backBitmap, upRect, baseRect,null)
//        return bitmap
//    }

    private fun mergeBitmap(
        firstImage: Bitmap,
        secondImage: Bitmap,
        thirdImage: Bitmap
    ): Bitmap? {
        val result = Bitmap.createBitmap(firstImage.width, firstImage.height, firstImage.config)
        val canvas = Canvas(result)
        canvas.drawBitmap(firstImage, 0f, 0f, null)
        canvas.drawBitmap(secondImage,50f, 10f, null)
        canvas.drawBitmap(thirdImage, 50f,800f, null)
        return result
    }

    private fun merge(view: View) {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis())
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        val uri =
            activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.apply {
            val ops = activity?.contentResolver?.openOutputStream(this)
            mergeBitmap(bgBitmap, binding.upperText.drawingCache, binding.bottomText.drawingCache)?.compress(Bitmap.CompressFormat.JPEG, 100, ops)
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