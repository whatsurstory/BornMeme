package com.beva.bornmeme.ui.gallery


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentGalleryBinding
import com.beva.bornmeme.model.Image
import com.bumptech.glide.Glide
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class GalleryFragment: Fragment() {

    private lateinit var binding: FragmentGalleryBinding
    private lateinit var viewModel:GalleryViewModel
    private lateinit var adapter: GalleryAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGalleryBinding.inflate(layoutInflater)
        viewModel = GalleryViewModel()
        adapter = GalleryAdapter(
            GalleryAdapter.OnClickListener {
                showDialog(it)
            }
        )

        binding.verticalRecycle.layoutManager = GridLayoutManager(this.context, 3)

        binding.verticalRecycle.adapter = adapter

        viewModel.liveData.observe(viewLifecycleOwner, Observer{
            Timber.d("imageItems observe, $it")
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })

        return binding.root
    }

    private fun showDialog (img:Image) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_image, null)
        builder.setView(view)
        val image = view.findViewById<ImageView>(R.id.gallery_img)
        Glide.with(image).load(img.url).placeholder(R.drawable._50).into(image)

        builder.setTitle("Is ${img.title} your final choice?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            val bitmapDrawable = image.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            saveImage(bitmap, img.imageId)
            Timber.d("filePath -> $bitmap")
        }
        builder.setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->

        })
        val alertDialog: AlertDialog = builder.create()
//        val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
//        val width = metrics.widthPixels
//        val height = metrics.heightPixels
        alertDialog.show()
    }

    private fun saveImage(image: Bitmap, id:String): String? {
        var savedImagePath: String? = null
        val imageFileName = "$id.jpg"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/BornMeme"
        )
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Add the image to the system gallery
            galleryAddPic(savedImagePath)

//            Toast.makeText(context, "IMAGE SAVED", Toast.LENGTH_LONG).show()
        }
        return savedImagePath
    }
    private fun galleryAddPic(imagePath: String) {
//        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val file = File(imagePath)
//        val contentUri = Uri.fromFile(file)
//        mediaScanIntent.data = contentUri
//        MediaScannerConnection.scanFile(context, arrayOf(file.toString()),
//            null, null)
//        Timber.d("contentUri $contentUri \n file $file ")

        val newUri =
            FileProvider.getUriForFile(
                requireContext(),
                "com.beva.bornmeme.fileProvider", file
            )
        Timber.d("fileUri -> $newUri")
        findNavController().navigate(MobileNavigationDirections.navigateToEditFragment(newUri))
    }

}



