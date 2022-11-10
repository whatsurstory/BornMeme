package com.beva.bornmeme.ui.detail.user.fragments.posts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.databinding.FragmentPostsBinding

class PostsFragment : Fragment() {


    private lateinit var viewModel: PostsViewModel
    private lateinit var binding: FragmentPostsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostsBinding.inflate(inflater,container,false)
        viewModel = PostsViewModel()

        binding.postRecycler.layoutManager = GridLayoutManager(context,3)
        val adapter = PostAdapter(
            PostAdapter.OnClickListener {
                viewModel.navigateToDetail(it)
            }
        )
        binding.postRecycler.adapter = adapter

        viewModel.postData.observe(viewLifecycleOwner) {
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