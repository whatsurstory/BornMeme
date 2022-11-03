package com.beva.bornmeme

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
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
import java.io.File

class MainActivity : AppCompatActivity() {
    private var saveUri: Uri? = null

    //TODO: ToolBar & ActionBar -> Stylish
    private var isOpen = false
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

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

        setSupportActionBar(binding.appBarMain.toolbar)

        //let the search view expend
        binding.appBarMain.searchBar.setOnClickListener {
            binding.appBarMain.searchBar.onActionViewExpanded()
        }

        //Animation of fab
        val fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        val fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        val fabRotate = AnimationUtils.loadAnimation(this, R.anim.rotate)
        val fabRotateAnti = AnimationUtils.loadAnimation(this, R.anim.rotate_anti)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        //TODO: changing R.id.fragment
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_home, R.id.fragmentEditFixmode
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //fab expending animation
        binding.appBarMain.fab.setOnClickListener { view ->

            if (isOpen) {
                binding.appBarMain.fabCameraEdit.startAnimation(fabClose)
                binding.appBarMain.fabModuleEdit.startAnimation(fabClose)
                binding.appBarMain.fabGalleryEdit.startAnimation(fabClose)
                binding.appBarMain.fab.startAnimation(fabRotate)

                isOpen = false
            } else {
                binding.appBarMain.fabCameraEdit.startAnimation(fabOpen)
                binding.appBarMain.fabModuleEdit.startAnimation(fabOpen)
                binding.appBarMain.fabGalleryEdit.startAnimation(fabOpen)
                binding.appBarMain.fab.startAnimation(fabRotateAnti)

                binding.appBarMain.fabCameraEdit.isClickable
                binding.appBarMain.fabModuleEdit.isClickable
                binding.appBarMain.fabGalleryEdit.isClickable

                isOpen = true
            }
            binding.appBarMain.fabCameraEdit.setOnClickListener {
                toCamera()
            }
            binding.appBarMain.fabModuleEdit.setOnClickListener {
                Snackbar.make(view, "This is Module Button", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
            }
            binding.appBarMain.fabGalleryEdit.setOnClickListener {
                toAlbum()
            }
        }
        //the tool bar showing only in the home fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.nav_home) {
                binding.appBarMain.toolbar.visibility = View.VISIBLE
            } else {
                binding.appBarMain.toolbar.visibility = View.GONE
            }
        }
    }

    //permission fun
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

    //if we got the photo from camera/gallery, we'll take the arguments of image complete the navigate
    private fun navigateToEditor(uri: Uri?) {
        uri?.let {
            findNavController(R.id.nav_host_fragment_content_main)
                .navigate(MobileNavigationDirections.navigateToEditFragment(it))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}