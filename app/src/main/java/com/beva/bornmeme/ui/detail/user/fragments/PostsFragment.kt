package com.beva.bornmeme.ui.detail.user.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.beva.bornmeme.R

class PostsFragment : Fragment() {

    companion object {
        fun newInstance() = PostsFragment()
    }

    private lateinit var viewModel: PostsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_posts, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PostsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}