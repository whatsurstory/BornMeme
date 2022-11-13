package com.beva.bornmeme.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
import androidx.recyclerview.widget.StaggeredGridLayoutManager.GAP_HANDLING_NONE
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
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
        viewModel = HomeViewModel()
        val layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerHome.layoutManager = layoutManager

        adapter = HomeAdapter(
            HomeAdapter.OnClickListener {
                viewModel.navigateToDetail(it)
            }
        )
        binding.recyclerHome.adapter = adapter
        layoutManager.gapStrategy = GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        viewModel.liveData.observe(viewLifecycleOwner, Observer {

            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
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
}






