package com.beva.bornmeme.ui.detail.user.fragments.comments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentCommentsBinding
import timber.log.Timber


class CommentsFragment : Fragment() {

    companion object {
        fun newInstance(userId: String): CommentsFragment {
            val fragment = CommentsFragment()
            val arg = Bundle()
            arg.putString("userIdKey", userId)
            fragment.arguments = arg
            return fragment
        }
    }

    lateinit var binding: FragmentCommentsBinding
    private lateinit var viewModel: CommentsViewModel
    //TODO: Single Recycler onClick to "the post" view

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommentsBinding.inflate(inflater, container, false)

        val userId = requireArguments().getString("userIdKey") ?: ""
        viewModel = CommentsViewModel(userId)

        val adapter = UserCommentAdapter()

        binding.commentRecycler.adapter = adapter

        viewModel.postData.observe(viewLifecycleOwner) {
            Timber.d("observe comment $it")
            adapter.submitList(it)
        }

        return binding.root
    }
}