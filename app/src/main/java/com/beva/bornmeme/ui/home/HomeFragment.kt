package com.beva.bornmeme.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
import androidx.recyclerview.widget.StaggeredGridLayoutManager.GAP_HANDLING_NONE
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.databinding.FragmentHomeBinding
import timber.log.Timber


class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: HomeAdapter



    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        //The Logic of Tag clicked
        setAllTagEnabled(false)

        viewModel = HomeViewModel()

        val layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)

        binding.recyclerHome.layoutManager = layoutManager

        adapter = HomeAdapter(
            HomeAdapter.OnClickListener {
                viewModel.navigateToDetail(it)
            },
        viewModel.uiState
        )

        binding.recyclerHome.adapter = adapter

        layoutManager.gapStrategy = GAP_HANDLING_NONE

        viewModel.display.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })

//        viewModel.display.observe(viewLifecycleOwner, Observer {
//            Timber.d("viewModel display value $it")
//          先藉由obeserve知道拿的資料是什麼
//        })

        val tagAdapter = TagAdapter (
            TagAdapter.OnClickListener {
                Timber.d("observe the click item $it")
                viewModel.changeTag(it)
                setAllTagEnabled(true)
            }
        )
        binding.chipRecycler.adapter = tagAdapter

        binding.resetBtn.setOnClickListener {

            tagAdapter.reset()
            viewModel.resetTag()

        }

        viewModel.tagCell.observe(viewLifecycleOwner, Observer {
            it?.let {
                Timber.d(("Observe cell : $it"))
                tagAdapter.submitList(it)
                tagAdapter.notifyDataSetChanged()
            }
        })


        //click to detail
        viewModel.navigateToDetail.observe(
            viewLifecycleOwner,
            Observer {
                it?.let {
                    findNavController().navigate(MobileNavigationDirections.navigateToImgDetailFragment(it))
                    viewModel.onDetailNavigated()
                }
            }
        )

        return binding.root
    }

    private fun setAllTagEnabled(isEnabled: Boolean) {
        binding.resetBtn.isEnabled = isEnabled
        binding.resetBtn.isChecked = !isEnabled
    }
}






