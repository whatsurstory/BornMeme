package com.beva.bornmeme.ui.detail.dialog

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MainActivity
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.DialogCommentBinding
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class PublishCommentDialog: AppCompatDialogFragment() {
//TODO: take the data to viewModel -> observe the argument -> complete the post comment with taking user data
    lateinit var binding: DialogCommentBinding
    lateinit var viewModel: PublishViewModel
    private lateinit var postId: String
    private lateinit var parentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.PublishDialog)
        binding = DialogCommentBinding.inflate(layoutInflater)
        binding.layoutPublish.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide))
        viewModel = PublishViewModel()
        binding.dialog = this
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        arguments?.let { bundle ->
            postId = bundle.getString("postId").toString()
            Timber.d("get postId from post $postId")
            parentId = bundle.getString("parentId").toString()
            Timber.d("get parentId from post $parentId")
        }

        if (parentId.isNotEmpty()) {
            binding.replyWhoText.visibility = View.VISIBLE
            viewModel.getUserName(parentId, binding)
        }


        binding.buttonPublish.setOnClickListener {

            if (binding.editPublishContent.text.isNullOrEmpty()) {
                Snackbar.make(it, "Not Adding Text Yet", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()

            } else {
                binding.postCommentLotties.visibility = View.VISIBLE
                binding.postCommentLotties.setAnimation(R.raw.refresh)
                viewModel.publishComment(postId, parentId ,binding)
                Handler(Looper.getMainLooper()).postDelayed({
                    findNavController().navigateUp()
                }, 1200)
            }
        }

        return binding.root
    }
}