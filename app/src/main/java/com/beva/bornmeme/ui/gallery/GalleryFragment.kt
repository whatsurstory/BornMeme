package com.beva.bornmeme.ui.gallery


import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import com.beva.bornmeme.MainViewModel
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentGalleryBinding
import com.beva.bornmeme.loadImage
import com.beva.bornmeme.model.Image
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class GalleryFragment : Fragment() {

    private lateinit var binding: FragmentGalleryBinding
    private lateinit var viewModel: GalleryViewModel
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
//                Timber.d("it -> ${it.imageId}")
            }
        )
        binding.verticalRecycle.layoutManager = GridLayoutManager(context, 3)
        binding.verticalRecycle.adapter = adapter

        viewModel.imageData.observe(viewLifecycleOwner, Observer {
//            Timber.d("imageItems observe, $it")
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
                "憤怒高登",
                "https://memeprod.ap-south-1.linodeobjects.com/user-template/58c44973669fb1f25f31fab113716c81.png"
            )
            document.set(publish)
        }
        return binding.root
    }

    private fun showDialog(img: Image) {
        val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_image, null)
        builder.setView(view)
        val image = view.findViewById<ImageView>(R.id.gallery_img)
        image.loadImage(img.url)

        builder.setMessage("就決定是${img.title}了嗎?(・∀・)つ⑩")
        builder.setPositiveButton(requireContext().getString(R.string.yes_btn)) { dialog, _ ->
            val bitmapDrawable = image.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            moduleCheckPermission(bitmap, img)
        }
        builder.setNegativeButton(requireContext().getString(R.string.no_btn),
            DialogInterface.OnClickListener { dialog, which -> })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            .setTextColor(requireContext().getColor(R.color.button_balck))
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setTextColor(requireContext().getColor(R.color.button_balck))
    }


    private fun moduleCheckPermission(bitmap: Bitmap, img: Image) {
        Dexter.withContext(requireContext()).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(
            object : PermissionListener {
                override fun onPermissionGranted(
                    p0: PermissionGrantedResponse?
                ) {
                    saveImage(bitmap, img.imageId)
                }

                override fun onPermissionDenied(
                    p0: PermissionDeniedResponse?
                ) {
                    Toast.makeText(
                        context,
                        getString(R.string.refuse_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                    showRotationDialogForPermission()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?, p1: PermissionToken?
                ) {
                    showRotationDialogForPermission()
                }
            }).onSameThread().check()
    }

    private fun saveImage(image: Bitmap, id: String): String? {
        var savedImagePath: String? = null
        val imageFileName = "$id.jpg"
        val storageDir = File(
            context?.filesDir,
            System.currentTimeMillis().toString() + ".jpg"
        )
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val outputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

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
//        Timber.d("fileUri -> $newUri")

        val mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
//        Timber.d("mainViewModel $mainViewModel")
        mainViewModel.editingImg = newUri
        findNavController().navigate(MobileNavigationDirections.navigateToEditFragment(newUri))
    }

    private fun showRotationDialogForPermission() {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.not_allow_prmission))

            .setPositiveButton(getString(R.string.to_setting_text)) { _, _ ->

                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", activity?.packageName, null)
                    intent.data = uri
                    startActivity(intent)

                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }

            .setNegativeButton(getString(R.string.cancel_text)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}



