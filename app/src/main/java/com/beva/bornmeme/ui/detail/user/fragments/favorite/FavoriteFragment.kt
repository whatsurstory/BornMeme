package com.beva.bornmeme.ui.detail.user.fragments.favorite

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentFavoriteBinding

class FavoriteFragment : Fragment() {


    private lateinit var viewModel: FavoriteViewModel
    private lateinit var binding: FragmentFavoriteBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteBinding.inflate(inflater,container,false)
        viewModel = FavoriteViewModel()

        binding.favoriteRecycler.layoutManager = GridLayoutManager(context,3)
        val adapter = FavoriteAdapter(
            FavoriteAdapter.OnClickListener {
                viewModel.navigateToDetail(it)
            }
        )
        binding.favoriteRecycler.adapter = adapter

        viewModel.likeData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

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