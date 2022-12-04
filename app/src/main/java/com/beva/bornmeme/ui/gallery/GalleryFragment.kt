package com.beva.bornmeme.ui.gallery


import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
                "憤怒高登",
                "https://memeprod.ap-south-1.linodeobjects.com/user-template/58c44973669fb1f25f31fab113716c81.png",
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
        Glide.with(image).load(img.url).placeholder(R.drawable.place_holder).into(image)

        builder.setMessage("就決定是${img.title}了嗎?(・∀・)つ⑩")
        builder.setPositiveButton("對沒錯") { dialog, _ ->
            val bitmapDrawable = image.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            moduleCheckPermission(bitmap, img)
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


    private fun moduleCheckPermission(bitmap: Bitmap, img: Image) {
        Dexter.withContext(requireContext()).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(
            object : PermissionListener {
                override fun onPermissionGranted(
                    p0: PermissionGrantedResponse?) {
                    saveImage(bitmap, img.imageId)
                }

                override fun onPermissionDenied(
                    p0: PermissionDeniedResponse?) {
                    Toast.makeText(
                        context,
                        "拒絕存取相簿權限",
                        Toast.LENGTH_SHORT
                    ).show()
                    showRotationDialogForPermission()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?, p1: PermissionToken?) {
                    showRotationDialogForPermission()
                }
            }).onSameThread().check()
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

    private fun showRotationDialogForPermission() {
        AlertDialog.Builder(requireContext())
            .setMessage("看起來你還沒有打開權限"
                    + "打開之後即可完整使用功能哦!")

            .setPositiveButton("前往設定") { _, _ ->

                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", activity?.packageName, null)
                    intent.data = uri
                    startActivity(intent)

                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }

            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}


//https://medium.com/@iansc/%E5%9C%A8-scoped-storage-%E5%AF%A6%E4%BD%9C%E4%B8%8B%E8%BC%89%E5%AA%92%E9%AB%94%E6%AA%94%E6%A1%88%E7%9A%84%E6%96%B9%E6%B3%95%E8%88%87%E7%B6%93%E9%A9%97%E5%88%86%E4%BA%AB-483d9250ba33
//suspend fun createImageUri(context: Context, fileName: String, dateTaken: Long): Uri? {
//    return withContext(Dispatchers.IO) {
//
//        // 使用 MediaStore 的 API 取得 ImageCollection 的 Content Uri
//        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
//        } else {
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        }
//
//        // 使用 ContentValue 對要儲存的圖片檔案進行設定
//        val newImage = ContentValues().apply {
//            // 檔案名
//            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
//            // 檔案類型
//            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//            // 檔案儲存時間
//            put(MediaStore.Images.Media.DATE_TAKEN, dateTaken)
//            // 檔案儲存路徑，API Level 需要在 29 以上，不指定會自動儲存在 Pictures 㡳下
//            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Sample")
//        }
//
//        // 透過 ContentResolver 產生並回傳圖片的 ContentUri
//        context.applicationContext.contentResolver.insert(imageCollection, newImage)
//    }
//}


