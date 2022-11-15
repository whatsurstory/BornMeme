package com.beva.bornmeme.ui.detail.user.fragments.collection

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentCollectionBinding
import com.beva.bornmeme.ui.home.HomeAdapter
import com.beva.bornmeme.ui.home.HomeViewModel
import org.checkerframework.checker.units.qual.C

class CollectionFragment : Fragment() {

    private lateinit var viewModel: CollectionViewModel
    private lateinit var binding:FragmentCollectionBinding
    private lateinit var adapter: CollectionAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCollectionBinding.inflate(inflater, container, false)
        viewModel = CollectionViewModel()

        adapter = CollectionAdapter(
            CollectionAdapter.OnClickListener {
                viewModel.navigateToDetail(it)
            }
        )
        binding.collectionRecycler.adapter = adapter

        viewModel.liveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })

        viewModel.navigateToDetail.observe(
            viewLifecycleOwner,
            Observer {
                it?.let {
                    findNavController().navigate(MobileNavigationDirections
                    .navigateToSlideDialog())
                    viewModel.onDetailNavigated()
                }
            }
        )

        return binding.root

    }

}