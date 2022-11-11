package com.beva.bornmeme.ui.detail.img


import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.MediaController
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentImgDetailBinding
import com.beva.bornmeme.model.Post
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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
            Timber.d("WelCome to Img Detail: arg -> ${post.id}")
        }

        binding.imgDetailUserName.text = post.ownerId

        Glide.with(this)
            .load(post.url)
            .apply(
            RequestOptions()
                .placeholder(R.drawable._50)
                .error(R.drawable.dino)
        ).into(binding.imgDetailImage)
        binding.imgDetailDescription.text = post.resources[1].url
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ImgDetailViewModel()
        viewModel.getComments(post.id)
        val adapter = CommentAdapter(viewModel.uiState)
        binding.commentsRecycler.adapter = adapter

        //Observe the view of comments recycler
        viewModel.commentCells.observe(viewLifecycleOwner, Observer {
            it?.let {
                Timber.d(("Observe comment cell : $it"))
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })

        //Query All Comments
        viewModel.liveData.observe(viewLifecycleOwner) {
            it?.let {
                Timber.d(("Observe liveData : $it"))
                viewModel.initCells(it)
            }
        }
        //OKです
        binding.imgDetailUserImg.setOnClickListener {
            findNavController().navigate(MobileNavigationDirections.navigateToUserDetailFragment())
        }
        //OKです
        binding.likeBtn.setOnClickListener {
            FirebaseFirestore.getInstance()
                .collection("Posts").document(post.id)
                .update("like", FieldValue.arrayUnion("cNXUG5FShzYesEOltXUZ"))
                .addOnSuccessListener {
                    Timber.d("Success add like")
                    Snackbar.make(this.requireView(), "喜歡喜歡~~~", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show()
                }.addOnFailureListener {
                    Timber.d("Error ${it.message}")
                }
        }
        //OKです
        binding.commentBtn.setOnClickListener {
            findNavController().navigate(MobileNavigationDirections
                .navigateToCommentDialog(postId = post.id))
        }
        //OKです
        viewModel.navigate2Comment.observe(viewLifecycleOwner) {
            Timber.d("Observe navigate $it")
            it?.let {
                findNavController().navigate(MobileNavigationDirections
                    .navigateToCommentDialog(postId = it.comment.postId, parentId = it.comment.commentId))
                viewModel.onDetailNavigated()
                Timber.d("navigate end")
            }
        }
        //OKです
        binding.followBtn.setOnClickListener {
            viewModel.onClickToFollow(post.ownerId)
        }

        binding.collectionBtn.setOnClickListener {
            val alert =  AlertDialog.Builder(requireContext())
            val edittext = EditText(requireContext())
            edittext.hint = "Enter Title Of Collection"
            edittext.maxLines = 1
            val layout = FrameLayout(requireContext())

            //set padding in parent layout
            layout.setPaddingRelative(45,15,45,0)
            layout.addView(edittext)
            alert.setView(layout)
            alert.setTitle("Name your Folder")

            alert.setPositiveButton("SAVE",
                DialogInterface.OnClickListener { dialog, which ->
                run {
                    val title = edittext.text.toString()
                    viewModel.onClickCollection(title, post.id, post.url.toString())
                    viewModel.doneCollection(post.id)
                }
            })
            alert.setNegativeButton("CANCEL"
            ) { _, _ ->
            }
            alert.show()
        }

        binding.shareBtn.setOnClickListener {
            val bitmapDrawable = binding.imgDetailImage.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            val bitmapPath = MediaStore.Images.Media.insertImage(requireContext().contentResolver, bitmap, "",null)
            val bitmapUri = Uri.parse(bitmapPath)

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
            startActivity(Intent.createChooser(intent, ""))
        }


        //BUG: the argument can't be rendering on newView
//        binding.imgDetailImage.setOnClickListener {
//            Timber.d(("index 0 => ${post.resources[0].url}"))
//            val uri : Uri = (post.resources[0].url.toString()).toUri()
//            findNavController().navigate(MobileNavigationDirections.navigateToEditFragment(uri))
//        }
        return binding.root
    }
}