package com.beva.bornmeme.ui.detail.img

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentImgDetailBinding
import com.beva.bornmeme.model.Post
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class ImgDetailFragment : Fragment() {

    private lateinit var binding: FragmentImgDetailBinding
    private lateinit var viewModel: ImgDetailViewModel
    private lateinit var post: Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentImgDetailBinding.inflate(layoutInflater)

                arguments?.let { bundle ->
            post = bundle.getParcelable("postKey")!!
            Log.d("Gallery", "get key = $post")
        }
        binding.imgDetailUserName.text = post.ownerId
        Glide.with(this)
            .load(post.url)
            .apply(
            RequestOptions()
//                .transform(CenterInside(), RoundedCorners(50))
                .placeholder(R.drawable._50)
                .error(R.drawable.dino)
        ).into(binding.imgDetailImage)
        binding.imgDetailDescription.text = post.resources[1].text
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ImgDetailViewModel()


        return binding.root
    }
}