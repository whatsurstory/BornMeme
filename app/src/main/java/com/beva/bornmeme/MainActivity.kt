package com.beva.bornmeme

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.beva.bornmeme.databinding.ActivityMainBinding
import com.beva.bornmeme.keyboard.StickerImporter
import com.beva.bornmeme.keyboard.Toaster
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
import java.util.Calendar
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    val viewModel by viewModels<MainViewModel> { getVmFactory() }

//    TODO: Move data to viewModel
    private var saveUri: Uri? = null

    private var isOpen = false
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    //Animation of fab
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation
    private lateinit var fabRotate: Animation
    private lateinit var fabRotateAnti: Animation

    private companion object {
        const val PHOTO_FROM_GALLERY = 0
        const val PHOTO_FROM_CAMERA = 1
    }

    private fun TextView.typeWrite(lifecycleOwner: LifecycleOwner, text: String, intervalMs: Long) {
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

        Timber.i("activity viewModel ${viewModel}")
        viewModel.user.observe(this, Observer{
            it?.let {
                Timber.d(("Observe User cell : $it"))
            Glide.with(this)
                .load(it.profilePhoto)
                .placeholder(R.drawable._50)
                .into(binding.profileBtn)
            }
        })

        //Animation of fab
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        fabRotate = AnimationUtils.loadAnimation(this, R.anim.rotate)
        fabRotateAnti = AnimationUtils.loadAnimation(this, R.anim.rotate_anti)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        //toolbar support action bar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.splash_screen, R.id.nav_home)) //  IDs of fragments you want without the ActionBar home/up button

        setupActionBarWithNavController(navController, appBarConfiguration)


        //the tool bar showing or not
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.splash_screen) {
                supportActionBar?.hide()
                binding.greeting.visibility = View.GONE
                binding.fab.visibility = View.GONE
                binding.profileBtn.visibility = View.GONE
                binding.changeModeBtn.visibility = View.GONE
            } else if (destination.id == R.id.fragmentEditFixmode || destination.id == R.id.dialogPreview) {
                supportActionBar?.show()
                binding.greeting.visibility = View.GONE
                binding.fab.visibility = View.GONE
                binding.profileBtn.visibility = View.GONE
                binding.moduleTitleText.visibility = View.GONE
                binding.changeModeBtn.visibility = View.VISIBLE
                binding.changeModeBtn.setOnClickListener {
                    Timber.d("你有按到change")
                    navigateToDrag(viewModel.editingImg)
                }
            } else  if (destination.id == R.id.fragmentEditDrag) {
                supportActionBar?.show()
                binding.changeModeBtn.visibility = View.GONE
                binding.fab.visibility = View.GONE
                binding.profileBtn.visibility = View.GONE
                binding.moduleTitleText.visibility = View.GONE
                binding.greeting.visibility = View.GONE
            } else {
                supportActionBar?.show()
                binding.greeting.visibility = View.GONE
                binding.fab.visibility = View.VISIBLE
                binding.profileBtn.visibility = View.GONE
                binding.changeModeBtn.visibility = View.GONE
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

                        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
                        fabRotate = AnimationUtils.loadAnimation(this, R.anim.rotate)
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

                        isOpen = false

                        cameraCheckPermission()
                    }
                    binding.fabModuleEdit.setOnClickListener {

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
                        navController.navigate(MobileNavigationDirections.navigateToFragmentGallery())
                    }
                    binding.fabGalleryEdit.setOnClickListener {

                        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
                        fabRotate = AnimationUtils.loadAnimation(this, R.anim.rotate)
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

                        isOpen = false

                        galleryCheckPermission()
                    }
                }
                if (destination.id == R.id.nav_home) {
                    viewModel.setUser(UserManager.user)
                    binding.moduleTitleText.visibility = View.GONE
                    binding.profileBtn.visibility = View.VISIBLE
                    binding.changeModeBtn.visibility = View.GONE
                    binding.greeting.visibility = View.VISIBLE
                    binding.greeting.typeWrite(this,
                        "BornMeme.",
                        100L)
                    binding.greeting.setOnClickListener {
                        navController.navigate(MobileNavigationDirections.navigateToFragmentSetting())
                    }
                } else if (destination.id == R.id.fragment_gallery) {
                    binding.moduleTitleText.visibility = View.VISIBLE
                } else {
                    binding.moduleTitleText.visibility = View.GONE
                }
            }
        }

        binding.profileBtn.setOnClickListener {
            Timber.d("userid ->")
            viewModel.user.value?.userId?.let { id ->
                navController.navigate(MobileNavigationDirections
                    .navigateToUserDetailFragment(id))
            }
        }

//        setupKeyboard()
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
//                val stickerDirPath = result.data?.data.toString()
//                editor.putString("stickerDirPath", stickerDirPath)
//                editor.putString("lastUpdateDate", Calendar.getInstance().time.toString())
//                editor.putString("recentCache", "")
//                editor.putString("compatCache", "")
//                editor.apply()
//                Timber.i("bmkb, stickerDirPath: $stickerDirPath")
//                val toaster = Toaster(baseContext)
//                val executor = Executors.newSingleThreadExecutor()
//                val handler = Handler(Looper.getMainLooper())
//                Toast.makeText(this, "嘻嘻", Toast.LENGTH_SHORT).show()
//                executor.execute {
//                    val totalStickers =
//                        StickerImporter(baseContext, toaster).importStickers(
//                            stickerDirPath
//                        )
//                    handler.post {
//                        toaster.toastOnState(
//                            arrayOf(
//                                getString(R.string.imported_020, totalStickers),
//                                getString(R.string.imported_031, totalStickers),
//                                getString(R.string.imported_032, totalStickers),
//                                getString(R.string.imported_033, totalStickers),
//                            )
//                        )
//                        editor.putInt("numStickersImported", totalStickers)
//                        editor.apply()
//
//                    }
//                }
//            }
//        }.launch(intent)
    }

//    fun setupKeyboard() {
//        Timber.i("bmkb, setupKeyboard")
//
//        val toaster = Toaster(baseContext)
//        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
//        var stickerDirPath =
//            Uri.parse(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath).toString() ?: ""
//
//
//        DocumentFile.fromFile().parentFile.uri
//
//        Timber.i("bmkb, stickerDirPath=$stickerDirPath")
//
////        stickerDirPath = "content://com.android.externalstorage.documents/tree/primary%3APictures"
//        editor.putString("stickerDirPath", stickerDirPath)
//        editor.putString("lastUpdateDate", Calendar.getInstance().time.toString())
//        editor.putString("recentCache", "")
//        editor.putString("compatCache", "")
//        editor.apply()
//
//        this.contentResolver.takePersistableUriPermission(
//            Uri.parse(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath),
//            Intent.FLAG_GRANT_READ_URI_PERMISSION
//        )
//
//        val executor = Executors.newSingleThreadExecutor()
//        val handler = Handler(Looper.getMainLooper())
//        Toast.makeText(this, "嘻嘻", Toast.LENGTH_SHORT).show()
//        executor.execute {
//            val totalStickers =
//                StickerImporter(baseContext, toaster).importStickers(
//                    stickerDirPath
//                )
//            handler.post {
//                toaster.toastOnState(
//                    arrayOf(
//                        getString(R.string.imported_020, totalStickers),
//                        getString(R.string.imported_031, totalStickers),
//                        getString(R.string.imported_032, totalStickers),
//                        getString(R.string.imported_033, totalStickers),
//                    )
//                )
//                editor.putInt("numStickersImported", totalStickers)
//                editor.apply()
//
//            }
//        }
//    }

//    fun updateUser(user: User) {
//        viewModel.setUser(user)
//    }


    private fun galleryCheckPermission() {
        Dexter.withContext(this).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(
            object : PermissionListener {
            override fun onPermissionGranted(
                p0: PermissionGrantedResponse?) {
                toAlbum()
            }

            override fun onPermissionDenied(
                p0: PermissionDeniedResponse?) {
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
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA).withListener(
                object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(
                        report: MultiplePermissionsReport?) {
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
                        Timber.d("PHOTO_FROM_CAMERA uri => $saveUri")
                        viewModel.editingImg = saveUri
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
        binding.fab.visibility = View.GONE
        uri?.let {
            findNavController(R.id.nav_host_fragment_content_main)
                .navigate(MobileNavigationDirections.navigateToEditFragment(it))
        }
    }

    @SuppressLint("RestrictedApi")
    private fun navigateToDrag(uri: Uri?) {
        binding.fab.visibility = View.GONE
        Timber.d("Uri -> $uri")
        uri?.let {
            findNavController(R.id.nav_host_fragment_content_main)
                .navigate(MobileNavigationDirections.navigateToDragEditFragment(it))

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

fun Activity.getVmFactory(): ViewModelFactory {
    return ViewModelFactory()
}