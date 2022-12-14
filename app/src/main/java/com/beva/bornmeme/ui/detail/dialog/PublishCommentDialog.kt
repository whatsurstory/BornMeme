package com.beva.bornmeme.ui.detail.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MainApplication
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.DialogCommentBinding
import kotlinx.coroutines.MainScope


class PublishCommentDialog : AppCompatDialogFragment() {

    lateinit var binding: DialogCommentBinding
    lateinit var viewModel: PublishViewModel
    private lateinit var postId: String
    private lateinit var parentId: String

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.PublishDialog)
        binding = DialogCommentBinding.inflate(layoutInflater)

        binding.layoutPublish.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide))

        viewModel = PublishViewModel()

        binding.dialog = this

        binding.editPublishContent.focusAndShowKeyboard()

    }

    fun View.focusAndShowKeyboard() {
        /**
         * This is to be called when the window already has focus.
         */
        fun View.showTheKeyboardNow() {
            if (isFocused) {
                post {
                    // We still post the call, just in case we are being notified of the windows focus
                    // but InputMethodManager didn't get properly setup yet.
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        requestFocus()
        if (hasWindowFocus()) {
            // No need to wait for the window to get focus.
            showTheKeyboardNow()
        } else {
            // We need to wait until the window gets focus.
            viewTreeObserver.addOnWindowFocusChangeListener(
                object : ViewTreeObserver.OnWindowFocusChangeListener {
                    override fun onWindowFocusChanged(hasFocus: Boolean) {
                        // This notification will arrive just before the InputMethodManager gets set up.
                        if (hasFocus) {
                            this@focusAndShowKeyboard.showTheKeyboardNow()
                            // It???s very important to remove this listener once we are done.
                            viewTreeObserver.removeOnWindowFocusChangeListener(this)
                        }
                    }
                })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let { bundle ->
            postId = bundle.getString("postId").toString()
            parentId = bundle.getString("parentId").toString()
        }

        if (parentId.isNotEmpty()) {
            binding.replyWhoText.visibility = View.VISIBLE
            viewModel.getUserName(parentId, binding, MainApplication.instance)
        }

        binding.buttonPublish.setOnClickListener {

            if (binding.editPublishContent.text.isNullOrEmpty()) {
                Toast.makeText(context, getString(R.string.input_yet), Toast.LENGTH_SHORT).show()
            } else {
                binding.postCommentLotties.visibility = View.VISIBLE
                binding.postCommentLotties.setAnimation(R.raw.refresh)
                viewModel.publishComment(postId, parentId, binding, MainApplication.instance)
                Handler(Looper.getMainLooper()).postDelayed({
                    findNavController().navigateUp()
                }, 1200)
            }
        }

        return binding.root
    }

}

