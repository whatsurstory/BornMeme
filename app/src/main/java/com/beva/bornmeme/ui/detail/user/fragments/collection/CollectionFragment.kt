package com.beva.bornmeme.ui.detail.user.fragments.collection

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentCollectionBinding
import com.beva.bornmeme.ui.home.HomeAdapter
import com.beva.bornmeme.ui.home.HomeViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.checkerframework.checker.units.qual.C
import timber.log.Timber

class CollectionFragment : Fragment() {

    companion object {
        fun newInstance(userId: String): CollectionFragment {
            val fragment = CollectionFragment()
            val arg = Bundle()
            arg.putString("userIdKey", userId)
            fragment.arguments = arg
            return fragment
        }
    }

    private lateinit var viewModel: CollectionViewModel
    private lateinit var binding:FragmentCollectionBinding
    private lateinit var adapter: CollectionAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCollectionBinding.inflate(inflater, container, false)

        val userId = requireArguments().getString("userIdKey") ?:""
        viewModel = CollectionViewModel(userId)

        adapter = CollectionAdapter(
            CollectionAdapter.OnClickListener {
                viewModel.navigateToDetail(it)
            }
        ,viewModel,userId
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
                    .navigateToSlideDialog(it))
                    viewModel.onDetailNavigated()
                }
            }
        )

        return binding.root
    }

}