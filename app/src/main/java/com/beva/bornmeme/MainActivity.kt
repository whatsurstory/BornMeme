package com.beva.bornmeme

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import coil.transform.CircleCropTransformation
import com.beva.bornmeme.MainActivity.Companion.PHOTO_FROM_CAMERA
import com.beva.bornmeme.MainActivity.Companion.PHOTO_FROM_GALLERY
import com.beva.bornmeme.databinding.ActivityMainBinding
import com.beva.bornmeme.ui.slideshow.OpenCameraFragment
import com.bumptech.glide.Glide
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
    private var saveUri: Uri? = null

    //TODO: ToolBar & ActionBar -> Stylish
    private var isOpen = false
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
//    private lateinit var navController: NavController


    private companion object {
        const val PHOTO_FROM_GALLERY = 0
        const val PHOTO_FROM_CAMERA = 1

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        if (savedInstanceState != null) {
            saveUri = Uri.parse(savedInstanceState.getString("saveUri"))
        }
        permission()
//        val viewModel = MainViewModel()
//        binding.lifecycleOwner = this
        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.searchBar.setOnClickListener {
            binding.appBarMain.searchBar.onActionViewExpanded()
        }


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
                toCamera()
            }
            binding.appBarMain.fabModule.setOnClickListener {
                Snackbar.make(view, "This is Module Button", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
            }
            binding.appBarMain.fabPhoto.setOnClickListener {
                toAlbum()
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.nav_home) {
                binding.appBarMain.toolbar.visibility = View.VISIBLE
            } else {
                binding.appBarMain.toolbar.visibility = View.GONE
            }
        }
    }


    private fun permission() {
        val permissionList = arrayListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        var size = permissionList.size
        var i = 0
        while (i < size) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permissionList[i]
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.removeAt(i)
                i -= 1
                size -= 1
            }
            i += 1
        }
        val array = arrayOfNulls<String>(permissionList.size)
        if (permissionList.isNotEmpty()) ActivityCompat.requestPermissions(
            this,
            permissionList.toArray(array),
            0
        )
    }

    private fun toAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PHOTO_FROM_GALLERY)
    }

    private fun toCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val tmpFile = File(
            Environment.getExternalStorageDirectory().toString(),
            System.currentTimeMillis().toString() + ".jpg"
        )
        val uriForCamera =
            FileProvider.getUriForFile(this, "com.beva.bornmeme.fileProvider", tmpFile)

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
        Log.d("uri", "onActivityResult, requestCode=$requestCode, resultCode=$resultCode")
        when (requestCode) {
            PHOTO_FROM_GALLERY -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val uri = data!!.data
                        Log.d("uri", "$uri")
                        navigateToEditor(uri)
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.d("getImageResult", resultCode.toString())
                    }
                }
            }

            PHOTO_FROM_CAMERA -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
//                        Glide.with(this).load(saveUri).into(binding.originPhoto)
                        Log.d("camera saveUri", "$saveUri")
//                        navigateToEditor(saveUri)
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.d("getPhotoResult", resultCode.toString())
                    }
                }
            }
        }
    }

    private fun navigateToEditor(uri: Uri?) {
        uri?.let {
            findNavController(R.id.nav_host_fragment_content_main)
                .navigate(MobileNavigationDirections.navigateToCameraFragment(it))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}