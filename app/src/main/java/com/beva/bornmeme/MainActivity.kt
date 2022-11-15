package com.beva.bornmeme

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
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
import com.beva.bornmeme.databinding.ActivityMainBinding
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
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
//    private var slidingNav: SlidingRootNav? = null

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

        setContentView(binding.root)
        if (savedInstanceState != null) {
            saveUri = Uri.parse(savedInstanceState.getString("saveUri"))
        }

        setSupportActionBar(binding.appBarMain.toolbar)

        //Animation of fab
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        fabRotate = AnimationUtils.loadAnimation(this, R.anim.rotate)
        fabRotateAnti = AnimationUtils.loadAnimation(this, R.anim.rotate_anti)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each menu should be considered as top level destinations.
        //if creating new menu must adding the id in navigation and menu
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,R.id.bottomsheet_edit_profile ,R.id.user_detail_fragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        binding.appBarMain.sortBtn.setOnClickListener {
            navController.navigate(MobileNavigationDirections.navigateToDragEditFragment())
        }
        //this is need to adding new layout view not really menu
//        slidingNav = SlidingRootNavBuilder(this)
//            .withMenuLayout(R.layout.nav_header_main)
//            .withToolbarMenuToggle(binding.appBarMain.toolbar)
//            .withMenuOpened(false)
//            .withContentClickableWhenMenuOpened(true)
//            .withSavedState(savedInstanceState)
//            .inject()

        //the tool bar showing or not
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.fragmentEditFixmode || destination.id == R.id.dialogPreview) {
                binding.appBarMain.toolbar.visibility = View.GONE
                binding.appBarMain.fab.visibility = View.GONE
                binding.appBarMain.fab.setOnClickListener { galleryCheckPermission() }
            } else {
                binding.appBarMain.toolbar.visibility = View.VISIBLE
                binding.appBarMain.fab.visibility = View.VISIBLE
                //fab expending animation
                binding.appBarMain.fab.setOnClickListener { view ->

                    if (isOpen) {
                        binding.appBarMain.fabCameraEdit.startAnimation(fabClose)
                        binding.appBarMain.fabModuleEdit.startAnimation(fabClose)
                        binding.appBarMain.fabGalleryEdit.startAnimation(fabClose)
                        binding.appBarMain.fab.startAnimation(fabRotate)

                        binding.appBarMain.fabCameraEdit.isEnabled = false
                        binding.appBarMain.fabModuleEdit.isEnabled = false
                        binding.appBarMain.fabGalleryEdit.isEnabled = false

                        isOpen = false
                    } else {

                        binding.appBarMain.fabCameraEdit.startAnimation(fabOpen)
                        binding.appBarMain.fabModuleEdit.startAnimation(fabOpen)
                        binding.appBarMain.fabGalleryEdit.startAnimation(fabOpen)
                        binding.appBarMain.fab.startAnimation(fabRotateAnti)

                        binding.appBarMain.fabCameraEdit.visibility = View.VISIBLE
                        binding.appBarMain.fabModuleEdit.visibility = View.VISIBLE
                        binding.appBarMain.fabGalleryEdit.visibility = View.VISIBLE

                        binding.appBarMain.fabCameraEdit.isEnabled = true
                        binding.appBarMain.fabModuleEdit.isEnabled = true
                        binding.appBarMain.fabGalleryEdit.isEnabled = true

                        isOpen = true
                    }
                    binding.appBarMain.fabCameraEdit.setOnClickListener {
                        cameraCheckPermission()
                    }
                    binding.appBarMain.fabModuleEdit.setOnClickListener {
                        Snackbar.make(view, "This is Module Button", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show()
                    }
                    binding.appBarMain.fabGalleryEdit.setOnClickListener {
                        galleryCheckPermission()

                    }
                }
                if (destination.id == R.id.nav_home) {
                    binding.appBarMain.searchBar.visibility = View.VISIBLE
                } else {
                    binding.appBarMain.searchBar.visibility = View.GONE
                }
            }
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
            binding.appBarMain.fabCameraEdit.startAnimation(fabClose)
            binding.appBarMain.fabModuleEdit.startAnimation(fabClose)
            binding.appBarMain.fabGalleryEdit.startAnimation(fabClose)
            binding.appBarMain.fab.startAnimation(fabRotate)
            isOpen = false

            findNavController(R.id.nav_host_fragment_content_main)
                .navigate(MobileNavigationDirections.navigateToEditFragment(it))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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