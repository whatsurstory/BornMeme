package com.beva.bornmeme.ui.detail.dialog

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
            SlideAdapter.OnClickListener {
//                val shareIntent = Intent(Intent.ACTION_SEND)
//                shareIntent.type = "text/plain"
//                val url = context?.packageName
//                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "BornMeme.")
//                shareIntent.putExtra(Intent.EXTRA_TEXT, url)
//                startActivity(Intent.createChooser(shareIntent, "choose one"))

//                findNavController()
//                    .navigate(MobileNavigationDirections
//                        .navigateToImgDetailFragment(it.id))

                downLoad("${it.id}.jpg", "BornMeme.", it.url)
                viewModel.navigateToDetail(it)
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
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, fileName)
        downloadManager.enqueue(request)

        Toast.makeText(context, "下載完成", Toast.LENGTH_SHORT).show()
    }

}