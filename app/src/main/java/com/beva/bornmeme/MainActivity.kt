package com.beva.bornmeme

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import coil.transform.CircleCropTransformation
import com.beva.bornmeme.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File

class MainActivity : AppCompatActivity() {

    //TODO: ToolBar & ActionBar -> Stylish
    private var isOpen = false
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var imgUri: Uri
    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){
        Toast.makeText(this, "$imgUri",Toast.LENGTH_LONG).show()
    }

//    private val CAMERA_REQUEST_CODE = 1
//    private val GALLERY_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
//        val viewModel = MainViewModel()
//        binding.lifecycleOwner = this
        setSupportActionBar(binding.appBarMain.toolbar)
//        binding.appBarMain.viewModel = viewModel
//        viewModel.nav2Camera.observe(
//            this,
//            Observer {
//                Log.d("observe", "$it")
//                it?.let {
//                    findNavController(R.id.fab_camera).navigate(R.id.navigate_to_camera_fragment)
//                    viewModel.onCameraNavigated()
//                }
//            }
//        )

        val fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        val fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        val fabRotate = AnimationUtils.loadAnimation(this, R.anim.rotate)
        val fabRotateAnti = AnimationUtils.loadAnimation(this, R.anim.rotate_anti)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_camera
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        imgUri = createImgUri()!!

        binding.appBarMain.fab.setOnClickListener { view ->

            if (isOpen) {
                binding.appBarMain.fabCamera.startAnimation(fabClose)
                binding.appBarMain.fabModule.startAnimation(fabClose)
                binding.appBarMain.fabPhoto.startAnimation(fabClose)
                binding.appBarMain.fab.startAnimation(fabRotate)

                isOpen = false
            } else {
                binding.appBarMain.fabCamera.startAnimation(fabOpen)
                binding.appBarMain.fabModule.startAnimation(fabOpen)
                binding.appBarMain.fabPhoto.startAnimation(fabOpen)
                binding.appBarMain.fab.startAnimation(fabRotateAnti)

                binding.appBarMain.fabCamera.isClickable
                binding.appBarMain.fabModule.isClickable
                binding.appBarMain.fabPhoto.isClickable

                isOpen = true
            }
            binding.appBarMain.fabCamera.setOnClickListener {
                contract.launch(imgUri)
//                cameraCheckPermission()
//                Snackbar.make(view, "This is Camera Button", Snackbar.LENGTH_SHORT)
//                    .setAction("Action", null).show()
            }
            binding.appBarMain.fabModule.setOnClickListener {
                Snackbar.make(view, "This is Module Button", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
            }
            binding.appBarMain.fabPhoto.setOnClickListener {
                val gallery =
                    Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(gallery, PICTUREFROMGALLERY)
//                galleryCheckPermission()
//                Snackbar.make(view, "This is Photo Button", Snackbar.LENGTH_SHORT)
//                    .setAction("Action", null).show()
            }
        }
    }

    private fun createImgUri(): Uri? {
        val img = File(applicationContext.filesDir, "camera_photo.png")
        return FileProvider.getUriForFile(applicationContext, "com.beva.bornmeme.fileProvider",img)
    }
//
//    private fun galleryCheckPermission() {
//        Dexter.withContext(this).withPermission(
//            android.Manifest.permission.READ_EXTERNAL_STORAGE
//        ).withListener(object : PermissionListener {
//            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
//                gallery()
//            }
//
//            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
//                Toast.makeText(
//                    this@MainActivity,
//                    "You have denied the storage permission to select image",
//                    Toast.LENGTH_SHORT
//                ).show()
//                showRotationDialogForPermission()
//            }
//
//            override fun onPermissionRationaleShouldBeShown(
//                p0: PermissionRequest?, p1: PermissionToken?) {
//                showRotationDialogForPermission()
//            }
//        }).onSameThread().check()
//    }
//
//    private fun gallery() {
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        startActivityForResult(intent, GALLERY_REQUEST_CODE)
//    }
//
//    private fun cameraCheckPermission() {
//        Dexter.withContext(this).withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA).withListener(
//            object : MultiplePermissionsListener{
//                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
//                    report.let {
//                        if (report != null) {
//                            if (report.areAllPermissionsGranted()){
//                                camera()
//                            }
//                        }
//                    }
//                }
//
//                override fun onPermissionRationaleShouldBeShown(
//                    p0: MutableList<PermissionRequest>?,
//                    p1: PermissionToken?
//                ) {
//                    showRotationDialogForPermission()
//                }
//
//            }
//        ).onSameThread().check()
//    }
//
//    private fun camera() {
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        startActivityForResult(intent, CAMERA_REQUEST_CODE)
//    }

    //Save Image Uri and send to CameraFragment to Show
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == Activity.RESULT_OK) {
//           when (resultCode) {
//               CAMERA_REQUEST_CODE -> {
//                   val bitmap = data?.extras?.get("data") as Bitmap
//                   Log.d("Camera Photo Uri", "$bitmap")
                   //we are using coroutine image loader (coil)
//                   binding.imageView.load(bitmap) {
//                       crossfade(true)
//                       crossfade(1000)
//                       transformations(CircleCropTransformation())
//                   }
//               GALLERY_REQUEST_CODE -> {
//                   Log.d("Gallery Photo Uri", "${data?.data}")
//                   binding.imageView.load(data?.data) {
//                       crossfade(true)
//                       crossfade(1000)
//                       transformations(CircleCropTransformation())
//                   }
//               }
//           }
//        }
//    }
//
//
//    private fun showRotationDialogForPermission() {
//        AlertDialog.Builder(this)
//            .setMessage("It looks like you have turned off permissions"
//                    + "required for this feature. It can be enable under App settings!!!")
//
//            .setPositiveButton("Go TO SETTINGS") { _, _ ->
//
//                try {
//                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                    val uri = Uri.fromParts("package", packageName, null)
//                    intent.data = uri
//                    startActivity(intent)
//
//                } catch (e: ActivityNotFoundException) {
//                    e.printStackTrace()
//                }
//            }
//
//            .setNegativeButton("CANCEL") { dialog, _ ->
//                dialog.dismiss()
//            }.show()
//    }
    companion object {
        const val PICTUREFROMGALLERY = 1001
        const val PICTUREFROMCAMERA = 1002
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}