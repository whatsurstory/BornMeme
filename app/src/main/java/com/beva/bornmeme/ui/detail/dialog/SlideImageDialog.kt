package com.beva.bornmeme.ui.detail.dialog

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.PagerSnapHelper
import com.beva.bornmeme.BuildConfig
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.DialogSlideCollectionBinding
import com.beva.bornmeme.model.Folder
import com.beva.bornmeme.model.FolderData
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import timber.log.Timber


class SlideImageDialog: AppCompatDialogFragment() {

    private lateinit var binding: DialogSlideCollectionBinding
    private lateinit var viewModel: SlideViewModel
    private lateinit var folder: Folder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.SlideDialog)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let { bundle ->
            folder = bundle.getParcelable("folder")!!
            Timber.d("WelCome to FOLDERRR: arg -> $folder")
        }
//        dialog?.setTransparentBackground()
        viewModel = SlideViewModel(folder)
        binding = DialogSlideCollectionBinding.inflate(layoutInflater)
        binding.dialog = this

        val adapter = SlideAdapter(
            SlideAdapter.OnClickListener { folder ->
//                val shareIntent = Intent(Intent.ACTION_SEND)
//                shareIntent.type = "text/plain"
//                val url = context?.packageName
//                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "BornMeme.")
//                shareIntent.putExtra(Intent.EXTRA_TEXT, url)
//                startActivity(Intent.createChooser(shareIntent, "choose one"))

//                findNavController()
//                    .navigate(MobileNavigationDirections
//                        .navigateToImgDetailFragment(it.id))

                checkPermission(folder)
                viewModel.navigateToDetail(folder)
            }
        )
        binding.slideImgRecycler.adapter = adapter
//        val snapHelper: SnapHelper = LinearSnapHelper()
//        snapHelper.attachToRecyclerView(binding.slideImgRecycler)

        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.slideImgRecycler)

        val indicator2 = binding.indicator
        indicator2.attachToRecyclerView(binding.slideImgRecycler, pagerSnapHelper)

        adapter.registerAdapterDataObserver(indicator2.adapterDataObserver)

        viewModel.imageItem.observe(viewLifecycleOwner, Observer {
            Timber.d("observe -> $it")
            adapter.submitList(it)
            adapter.notifyDataSetChanged()

        })

        viewModel.navigateToDetail.observe(
            viewLifecycleOwner,
            Observer {
                it?.let {
//                    findNavController().navigate(MobileNavigationDirections.navigateToImgDetailFragment(it.id))
                    viewModel.onDetailNavigated()
                }
            }
        )

        return binding.root
    }

    private fun checkPermission(folder: FolderData) {
        Dexter.withContext(requireContext()).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(
            object : PermissionListener {
                override fun onPermissionGranted(
                    p0: PermissionGrantedResponse?) {
                    downLoad("${folder.id}.jpg", "BornMeme.", folder.url)
//                    viewModel.downloadImage(folder.url)

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

    private fun downLoad(fileName: String, desc: String, url: String) {
        Timber.d("file name $fileName")
        val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            .setTitle(fileName)
            .setDescription(desc)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        downloadManager.enqueue(request)
        Toast.makeText(context, "下載完成", Toast.LENGTH_SHORT).show()
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