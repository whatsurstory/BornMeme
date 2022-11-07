package com.beva.bornmeme.ui.detail.img


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentImgDetailBinding
import com.beva.bornmeme.model.Post
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.circleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import timber.log.Timber

class ImgDetailFragment : Fragment() {

    private lateinit var binding: FragmentImgDetailBinding
    private lateinit var viewModel: ImgDetailViewModel
    private lateinit var post: Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentImgDetailBinding.inflate(layoutInflater)
                arguments?.let { bundle ->
            post = bundle.getParcelable("postKey")!!
            Timber.d("detail get key from home= $post")
        }

        binding.imgDetailUserName.text = post.ownerId

        Glide.with(this)
            .load(post.url)
            .transform(CenterInside(), RoundedCorners(50))
            .apply(
            RequestOptions()
                .placeholder(R.drawable._50)
                .error(R.drawable.dino)
        ).into(binding.imgDetailImage)
        binding.imgDetailDescription.text = post.resources[1].url
        Timber.d("index 1 => ${post.resources[1].url}")
        //TODO: Navigate use this image as a Module
//        binding.imgDetailImage.setOnClickListener {
//            findNavController().navigate(MobileNavigationDirections.navigateToEditFragment(post.resources[0].url))
//        }
        Timber.d(("index 0 => ${post.resources[0].url}"))

        binding.commentBtn.setOnClickListener {
            findNavController().navigate(MobileNavigationDirections.navigateToCommentDialog())
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ImgDetailViewModel()
        val adapter = CommentAdapter()
        binding.commentsRecycler.adapter =CommentAdapter()

        binding.imgDetailUserImg.setOnClickListener {
            findNavController().navigate(MobileNavigationDirections.navigateToUserDetailFragment())
        }
        return binding.root
    }
}