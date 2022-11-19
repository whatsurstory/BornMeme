package com.beva.bornmeme

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.os.UserManager
import android.provider.MediaStore
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SlidingDrawer
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import com.beva.bornmeme.databinding.ActivityMainBinding
import com.beva.bornmeme.model.UserManager.user
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.core.Context
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity() {

//    TODO: Move data to viewModel
    private var saveUri: Uri? = null

    private var isOpen = false
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    //Animation of fab
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation
    private lateinit var fabRotate: Animation
    private lateinit var fabRotateAnti: Animation

    private companion object {
        const val PHOTO_FROM_GALLERY = 0
        const val PHOTO_FROM_CAMERA = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        binding = ActivityMainBinding.inflate(layoutInflater)

        val userId = user.userId
        Timber.d("userid -> $userId")
        viewModel = MainViewModel(userId.toString())

        viewModel.userData.observe(this, Observer{
            it?.let {
                Timber.d(("Observe User cell : $it"))
            Glide.with(this)
                .load(it[0].profilePhoto)
                .placeholder(R.drawable._50)
                .into(binding.profileBtn)
            }
        })

        setContentView(binding.root)

        //Animation of fab
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        fabRotate = AnimationUtils.loadAnimation(this, R.anim.rotate)
        fabRotateAnti = AnimationUtils.loadAnimation(this, R.anim.rotate_anti)

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        if (com.beva.bornmeme.model.UserManager.isLoggedIn) {
            navController.navigate(MobileNavigationDirections.navigateToHomeFragment())
        }

        //the tool bar showing or not
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.splash_screen) {
                binding.fab.visibility = View.GONE
                binding.changeModeBtn.visibility = View.GONE
            } else if (destination.id == R.id.fragmentEditFixmode || destination.id == R.id.dialogPreview) {
                binding.fab.visibility = View.GONE
                binding.searchBar.visibility = View.GONE
                binding.changeModeBtn.visibility = View.VISIBLE
//                binding.changeModeBtn.setOnClickListener {
//                    Timber.d("你有按到change")
//                    navController.navigate(MobileNavigationDirections.navigateToDragEditFragment())
//                }
                binding.fab.setOnClickListener { toAlbum() }
            } else {
                binding.fab.visibility = View.VISIBLE
                binding.changeModeBtn.visibility= View.GONE
                //fab expending animation
                binding.fab.setOnClickListener { view ->

                    if (isOpen) {
                        binding.fabCameraEdit.startAnimation(fabClose)
                        binding.fabModuleEdit.startAnimation(fabClose)
                        binding.fabGalleryEdit.startAnimation(fabClose)
                        binding.fab.startAnimation(fabRotate)

                        binding.fabCameraEdit.visibility = View.GONE
                        binding.fabCameraEdit.clearAnimation()
                        binding.fabModuleEdit.visibility = View.GONE
                        binding.fabModuleEdit.clearAnimation()
                        binding.fabGalleryEdit.visibility = View.GONE
                        binding.fabGalleryEdit.clearAnimation()

                        binding.fabCameraEdit.isEnabled = false
                        binding.fabModuleEdit.isEnabled = false
                        binding.fabGalleryEdit.isEnabled = false

                        isOpen = false
                    } else {

                        binding.fabCameraEdit.startAnimation(fabOpen)
                        binding.fabModuleEdit.startAnimation(fabOpen)
                        binding.fabGalleryEdit.startAnimation(fabOpen)
                        binding.fab.startAnimation(fabRotateAnti)

                        binding.fabCameraEdit.visibility = View.VISIBLE
                        binding.fabModuleEdit.visibility = View.VISIBLE
                        binding.fabGalleryEdit.visibility = View.VISIBLE

                        binding.fabCameraEdit.isEnabled = true
                        binding.fabModuleEdit.isEnabled = true
                        binding.fabGalleryEdit.isEnabled = true

                        isOpen = true
                    }
                    binding.fabCameraEdit.setOnClickListener {
                        cameraCheckPermission()
                    }
                    binding.fabModuleEdit.setOnClickListener {
                        Snackbar.make(view, "This is Module Button", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show()
                    }
                    binding.fabGalleryEdit.setOnClickListener {
                        galleryCheckPermission()
                    }
                }
                if (destination.id == R.id.nav_home) {
                    binding.searchBar.visibility = View.VISIBLE
                    binding.changeModeBtn.visibility = View.GONE
                } else {
                    binding.searchBar.visibility = View.GONE
                }

            }
        }
        //toolbar support action bar
        setSupportActionBar(binding.toolbar)
        //navigate_up
        setupActionBarWithNavController(navController)

        binding.profileBtn.setOnClickListener {
            navController.navigate(MobileNavigationDirections.navigateToUserDetailFragment(
                userId.toString()
            ))
        }

    }






    private fun galleryCheckPermission() {
        Dexter.withContext(this).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : PermissionListener {
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                toAlbum()
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                Toast.makeText(
                    this@MainActivity,
                    "You have denied the storage permission to select image",
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

    private fun toAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PHOTO_FROM_GALLERY)
    }

    private fun cameraCheckPermission() {
        Dexter.withContext(this).withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA).withListener(
            object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report.let {
                        if (report != null) {
                            if (report.areAllPermissionsGranted()){
                                toCamera()
                            }
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    showRotationDialogForPermission()
                }

            }
        ).onSameThread().check()
    }

    private fun toCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val tmpFile = File(
            applicationContext.filesDir,
            System.currentTimeMillis().toString() + ".jpg"
        )

        val uriForCamera =
            FileProvider.getUriForFile(
                applicationContext,
                "com.beva.bornmeme.fileProvider",
                tmpFile
            )

        saveUri = uriForCamera
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForCamera)
        startActivityForResult(intent, PHOTO_FROM_CAMERA)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (saveUri != null) {
            val uriString = saveUri.toString()
            outState.putString("saveUri", uriString)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("onActivityResult requestCode => $requestCode resultCode => $resultCode")
        when (requestCode) {
            PHOTO_FROM_GALLERY -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val uri = data!!.data
                        Timber.d("PHOTO_FROM_GALLERY uri => $uri")
                        navigateToEditor(uri)
                    }
                    Activity.RESULT_CANCELED -> {
                        Timber.d("getImageResult cancel $resultCode")
                    }
                }
            }

            PHOTO_FROM_CAMERA -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Timber.d("PHOTO_FROM_CAMERA uri => $saveUri")
                        navigateToEditor(saveUri)
                    }
                    Activity.RESULT_CANCELED -> {
                        Timber.d("getPhotoResult cancel $resultCode")
                    }
                }
            }
        }
    }


    //if we got the photo from camera/gallery, we'll take the arguments of image complete the navigate
    private fun navigateToEditor(uri: Uri?) {
        uri?.let {
            fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
            fabRotate = AnimationUtils.loadAnimation(this, R.anim.rotate)
            binding.fabCameraEdit.startAnimation(fabClose)
            binding.fabModuleEdit.startAnimation(fabClose)
            binding.fabGalleryEdit.startAnimation(fabClose)
            binding.fab.startAnimation(fabRotate)
            isOpen = false

            findNavController(R.id.nav_host_fragment_content_main)
                .navigate(MobileNavigationDirections.navigateToEditFragment(it))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun showRotationDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permissions"
                    + "required for this feature. It can be enable under App settings!!!")

            .setPositiveButton("Go TO SETTINGS") { _, _ ->

                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)

                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }

            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}