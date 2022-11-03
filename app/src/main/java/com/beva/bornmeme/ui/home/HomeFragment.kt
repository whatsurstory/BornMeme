package com.beva.bornmeme.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentHomeBinding

import com.beva.bornmeme.model.Post
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: HomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = HomeViewModel()
        //lifecycle is works in databinding
        //binding.lifecycleOwner = viewLifecycleOwner

        binding.recyclerHome.apply {
            layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
        }


        adapter = HomeAdapter()
        binding.recyclerHome.adapter = adapter

        viewModel.liveData.observe(viewLifecycleOwner, Observer {
            Log.d("viewModel.liveArticles.observe"," it= $it")
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }

}

//        binding.recyclerHome.adapter = HomeAdapter(
//            HomeAdapter.OnClickListener {
//                viewModel.navigateToDetail(it)
//            }
//        )


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