package com.beva.bornmeme.ui.detail.dialog

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.PagerSnapHelper
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

}