package com.beva.bornmeme.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        //val args = HomeFragmentArgs.fromBundle(requireArguments()).photoKey
        binding.lifecycleOwner = viewLifecycleOwner

//        binding.recyclerHome.adapter = HomeAdapter(
//            HomeAdapter.OnClickListener {
//                viewModel.navigateToDetail(it)
//            }
//        )
        binding.recyclerHome.adapter = HomeAdapter()
        binding.recyclerHome.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
        }
//        if (args != null) {
//            viewModel.getPostResult(args)
//        }

        //click to detail
//        viewModel.navigateToDetail.observe(
//            viewLifecycleOwner,
//            Observer {
//                it?.let {
//                    findNavController().navigate(NavigationDirections.navigateToDetailFragment(it))
//                    viewModel.onDetailNavigated()
//                }
//            }
//        )

        //check if more item
//        binding.recyclerHome.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                if (dy > 0) {
//                    // 成功滑到bottom
//                    if (recyclerView.canScrollVertically(-1) && dy > 0) {
//                        if (args != null) {
//                            viewModel.getPostResult(args)
//                        }
//                    }
//                }
//                super.onScrolled(recyclerView, dx, dy)
//            }
//        })
        return binding.root
    }

}