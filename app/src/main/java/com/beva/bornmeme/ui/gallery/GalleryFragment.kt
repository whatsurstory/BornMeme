package com.beva.bornmeme.ui.gallery


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.beva.bornmeme.MainViewModel
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentGalleryBinding
import com.beva.bornmeme.model.Image
import com.beva.bornmeme.model.UserManager
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
                Timber.d("it -> ${it.imageId}")
            }
        )
        binding.verticalRecycle.layoutManager = GridLayoutManager(context, 3)
        binding.verticalRecycle.adapter = adapter

        viewModel.liveData.observe(viewLifecycleOwner, Observer{
            Timber.d("imageItems observe, $it")
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })


        //加照片用
        binding.addNewImgBtn.visibility = View.GONE
        binding.addNewImgBtn.setOnClickListener {
            val fireStore = FirebaseFirestore.getInstance().collection("Modules")
            val document = fireStore.document()
            val publish = Image(
                document.id,
                "我大哥",
                "https://memeprod.ap-south-1.linodeobjects.com/user-template/1bd1165b953a44c9ef68761073dfbe1d.png",
                emptyList(),
                emptyList()
            )
            document.set(publish)
        }
        return binding.root
    }

    private fun showDialog (img:Image) {
        val builder = AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_image, null)
        builder.setView(view)
        val image = view.findViewById<ImageView>(R.id.gallery_img)
        Glide.with(image).load(img.url).placeholder(R.drawable._50).into(image)

        builder.setMessage("就決定是${img.title}了嗎?(・∀・)つ⑩")
        builder.setPositiveButton("對沒錯") { dialog, _ ->
            val bitmapDrawable = image.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            saveImage(bitmap, img.imageId)
            Timber.d("filePath -> $bitmap")
        }
        builder.setNegativeButton("再想想", DialogInterface.OnClickListener { dialog, which ->

        })
        val alertDialog: AlertDialog = builder.create()
//        val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
//        val width = metrics.widthPixels
//        val height = metrics.heightPixels
        alertDialog.show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#181A19"))
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#181A19"))
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

        }
        return savedImagePath
    }
    private fun galleryAddPic(imagePath: String) {
        val file = File(imagePath)
        val newUri =
            FileProvider.getUriForFile(
                requireContext(),
                "com.beva.bornmeme.fileProvider", file
            )
        Timber.d("fileUri -> $newUri")

        val mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        Timber.d("mainViewModel $mainViewModel")
        mainViewModel.editingImg = newUri
        findNavController().navigate(MobileNavigationDirections.navigateToEditFragment(newUri))
    }

}



