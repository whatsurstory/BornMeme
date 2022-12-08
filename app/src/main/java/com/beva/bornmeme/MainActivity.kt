package com.beva.bornmeme

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.beva.bornmeme.databinding.ActivityMainBinding
import com.beva.bornmeme.model.UserManager
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity() {

    val viewModel by viewModels<MainViewModel> { getVmFactory() }

    private var cameraUri: Uri? = null
    private var isOpen = false
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    //Animation of fab
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation
    private lateinit var fabRotate: Animation
    private lateinit var fabAntiRotate: Animation

    private companion object {
        const val PHOTO_FROM_GALLERY = 0
        const val PHOTO_FROM_CAMERA = 1
    }

    //TextView Animation
    private fun TextView.typeWrite(
        lifecycleOwner: LifecycleOwner,
        text: String,
        intervalMs: Long
    ) {
        this@typeWrite.text = ""
        lifecycleOwner.lifecycleScope.launch {
            repeat(text.length) {
                delay(intervalMs)
                this@typeWrite.text = text.take(it + 1)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.user.observe(this, Observer {
            it?.let {
                binding.profileBtn.loadImage(it.profilePhoto)
            }
        })

        //Animation of fab
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        fabRotate = AnimationUtils.loadAnimation(this, R.anim.rotate)
        fabAntiRotate = AnimationUtils.loadAnimation(this, R.anim.rotate_anti)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        //toolbar support action bar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        appBarConfiguration = AppBarConfiguration(setOf(R.id.splash_screen, R.id.nav_home))
        //  IDs of fragments you want without the ActionBar home/up button
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splash_screen -> {
                    setToolbarUi(
                        isShowFab = false,
                        isShowActionBarShow = false
                    )
                }
                R.id.nav_home -> {
                    setToolbarUi(
                        isShowProfile = true
                    )
                    viewModel.setUser(UserManager.user)
                }
                R.id.fragmentEditFixmode -> {
                    setToolbarUi(
                        isShowFab = false,
                        isShowEditMode = true
                    )
                }
                R.id.fragmentEditDrag -> {
                    setToolbarUi(
                        isShowFab = false
                    )
                }
                R.id.fragment_gallery -> {
                    setToolbarUi(
                        isShowGalleryTitle = true
                    )
                }
                R.id.img_detail_fragment -> {
                    setToolbarUi()
                }
                R.id.user_detail_fragment -> {
                    setToolbarUi()
                }
            }
        }

        binding.fab.setOnClickListener { view ->

            if (isOpen) closeFab() else openFab()

            binding.fabCameraEdit.setOnClickListener {
                closeFab()
                Dexter.withContext(this)
                    .withPermissions(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA
                    )
                    .withListener(
                        cameraPermissionListener
                    )
                    .onSameThread()
                    .check()
            }
            binding.fabModuleEdit.setOnClickListener {
                closeFab()
                findNavController(R.id.nav_host_fragment_content_main)
                    .navigate(MobileNavigationDirections.navigateToFragmentGallery())
            }
            binding.fabGalleryEdit.setOnClickListener {
                closeFab()
                Dexter.withContext(this)
                    .withPermission(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    .withListener(albumPermissionListener)
                    .onSameThread()
                    .check()
            }
        }

        binding.changeModeBtn.setOnClickListener {
            navigateToDrag(viewModel.editingImg)
        }

        binding.profileBtn.setOnClickListener {
            viewModel.user.value?.userId?.let { id ->
                findNavController(R.id.nav_host_fragment_content_main)
                    .navigate(
                        MobileNavigationDirections
                            .navigateToUserDetailFragment(id)
                    )
            }
        }
    }


    private fun changeHomeTitle(isGreetingShow: Boolean, text: String?) {
        if (isGreetingShow) {
            binding.greeting.visibility = View.VISIBLE
            if (text != null) {
                binding.greeting.typeWrite(
                    this,
                    text,
                    100L
                )
            }
        } else {
            binding.greeting.visibility = View.GONE
        }
    }

    private fun setToolbarUi(
        isShowFab: Boolean = true,
        isShowEditMode: Boolean = false,
        isShowGalleryTitle: Boolean = false,
        isShowProfile: Boolean = false,
        isShowActionBarShow: Boolean = true
    ) {

        binding.profileBtn.visibility = if (isShowProfile) View.VISIBLE else View.GONE
        changeHomeTitle(isShowProfile, getString(R.string.bornmeme))
        binding.moduleTitleText.visibility = if (isShowGalleryTitle) View.VISIBLE else View.GONE
        binding.changeModeBtn.visibility = if (isShowEditMode) View.VISIBLE else View.GONE
        binding.fab.visibility = if (isShowFab) View.VISIBLE else View.GONE
        if (isShowActionBarShow) supportActionBar?.show() else supportActionBar?.hide()

    }

//    fun updateUser(user: User) {
//        viewModel.setUser(user)
//    }

    private val albumPermissionListener =
        object : PermissionListener {
            override fun onPermissionGranted(
                p0: PermissionGrantedResponse?
            ) {
                toAlbum()
            }

            override fun onPermissionDenied(
                p0: PermissionDeniedResponse?
            ) {
                showRotationDialogForPermission()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?, p1: PermissionToken?
            ) {
                showRotationDialogForPermission()
            }
        }

    private val cameraPermissionListener =
        object : MultiplePermissionsListener {
            override fun onPermissionsChecked(
                report: MultiplePermissionsReport?
            ) {
                if (report != null && report.areAllPermissionsGranted()) {
                    toCamera()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<PermissionRequest>?,
                p1: PermissionToken?
            ) {
                showRotationDialogForPermission()
            }
        }

    private fun toAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PHOTO_FROM_GALLERY)
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

        cameraUri = uriForCamera
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForCamera)
        startActivityForResult(intent, PHOTO_FROM_CAMERA)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (cameraUri != null) {
            val uriString = cameraUri.toString()
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
                        val uri = data?.data
                        viewModel.editingImg = uri
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
                        viewModel.editingImg = cameraUri
                        navigateToEditor(cameraUri)
                    }
                    Activity.RESULT_CANCELED -> {
                        Timber.d("getPhotoResult cancel $resultCode")
                    }
                }
            }
        }
    }

    //got the photo from camera/gallery, take the arguments of image complete the navigate
    private fun navigateToEditor(uri: Uri?) {
        uri?.let {
            findNavController(R.id.nav_host_fragment_content_main)
                .navigate(MobileNavigationDirections.navigateToEditFragment(it))
        }
    }

    @SuppressLint("RestrictedApi")
    private fun navigateToDrag(uri: Uri?) {
        uri?.let {
            findNavController(R.id.nav_host_fragment_content_main)
                .navigate(
                    MobileNavigationDirections
                        .navigateToDragEditFragment(it)
                )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            android.R.id.home -> {
                this.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun showRotationDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage(
                getString(R.string.not_allow_prmission)
            )
            .setPositiveButton(getString(R.string.to_setting_text)) { _, _ ->

                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
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

    //TODO
    private fun closeFab() {
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
    }

    private fun openFab() {
        binding.fabCameraEdit.startAnimation(fabOpen)
        binding.fabModuleEdit.startAnimation(fabOpen)
        binding.fabGalleryEdit.startAnimation(fabOpen)
        binding.fab.startAnimation(fabAntiRotate)

        binding.fabCameraEdit.visibility = View.VISIBLE
        binding.fabModuleEdit.visibility = View.VISIBLE
        binding.fabGalleryEdit.visibility = View.VISIBLE

        binding.fabCameraEdit.isEnabled = true
        binding.fabModuleEdit.isEnabled = true
        binding.fabGalleryEdit.isEnabled = true

        isOpen = true
    }
}

fun getVmFactory(): ViewModelFactory {
    return ViewModelFactory()
}

fun ImageView.loadImage(url: String?) {
    Glide.with(context)
        .load(url)
        .placeholder(R.drawable.place_holder)
        .into(this)
}