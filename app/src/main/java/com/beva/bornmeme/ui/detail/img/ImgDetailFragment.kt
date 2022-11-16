package com.beva.bornmeme.ui.detail.img


import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color.blue
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentImgDetailBinding
import com.beva.bornmeme.model.Folder
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
//        Timber.d("check User data ${post.user}")
        binding.imgDetailUserName.text = post.ownerId
        binding.imgDetailTitle.text = post.title
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
        viewModel = ImgDetailViewModel(post.ownerId)
        viewModel.getComments(post.id)

        viewModel.userData.observe(viewLifecycleOwner, Observer {
            it?.let {
                Glide.with(this)
                    .load(it[0].profilePhoto)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable._50)
                            .error(R.drawable.dino)
                    ).into(binding.imgDetailUserImg)
                binding.imgDetailUserName.text = it[0].userName
            }
        })

        binding.imgDetailTitle.text = post.title
        Glide.with(this)
            .load(post.url)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable._50)
                    .error(R.drawable.dino)
            ).into(binding.imgDetailImage)
        binding.imgDetailDescription.text = post.resources[1].url

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
        //the button to see user information
        binding.imgDetailUserImg.setOnClickListener {
            findNavController().navigate(MobileNavigationDirections
                .navigateToUserDetailFragment(post.ownerId))
        }
        //the button to like post
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
        //the button to post comments
        binding.commentBtn.setOnClickListener {
            findNavController().navigate(MobileNavigationDirections
                .navigateToCommentDialog(postId = post.id))
        }

        viewModel.navigate2Comment.observe(viewLifecycleOwner) {
            Timber.d("Observe navigate $it")
            it?.let {
                findNavController().navigate(MobileNavigationDirections
                    .navigateToCommentDialog(postId = it.comment.postId, parentId = it.comment.commentId))
                viewModel.onDetailNavigated()
                Timber.d("navigate end")
            }
        }

        //the button to follow other users
        binding.followBtn.setOnClickListener {
            viewModel.onClickToFollow(post.ownerId, binding)
        }

        viewModel.folderData.observe(viewLifecycleOwner, Observer {
            it?.let {
                showAlert(it)
            }
        })
        //the button to take post to collection
        binding.collectionBtn.setOnClickListener {
            Toast.makeText(context,"let's relax a moment ...",Toast.LENGTH_SHORT).show()
            viewModel.getFolder()
        }

        //the menu button to report and other feature
        val popupMenu = PopupMenu(context,
        binding.reportBtn)
        popupMenu.menu.add(Menu.NONE, 0,0,"Report the Image")
        popupMenu.menu.add(Menu.NONE,1,1,"Report the User")
        popupMenu.menu.add(Menu.NONE,2,2,"test")

        popupMenu.setOnMenuItemClickListener {
            when(val id = it.itemId) {
                0 -> Toast.makeText(context, "ID $id -> Report the Image",Toast.LENGTH_SHORT).show()
                1 -> Toast.makeText(context, "ID $id -> Report the User",Toast.LENGTH_SHORT).show()
                2 -> Toast.makeText(context, "ID $id -> Report the test",Toast.LENGTH_SHORT).show()
            }
            false
        }
        binding.reportBtn.setOnClickListener {
            popupMenu.show()
        }


        //the permission problem in samsung, maybe due to the version of android
        binding.shareBtn.setOnClickListener {
            val bitmapDrawable = binding.imgDetailImage.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            val bitmapPath = MediaStore.Images.Media.insertImage(requireContext()
                .contentResolver, bitmap, "Description",null)
            val bitmapUri = Uri.parse(bitmapPath)

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
            startActivity(Intent.createChooser(intent, "Description"))
        }


        //BUG: the argument can't be rendering on newView
//        binding.imgDetailImage.setOnClickListener {
//            Timber.d(("index 0 => ${post.resources[0].url}"))
//            val uri : Uri = (post.resources[0].url.toString()).toUri()
//            findNavController().navigate(MobileNavigationDirections.navigateToEditFragment(uri))
//        }
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    private fun showAlert(folderNames: List<String>) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        // Get the layout inflater
        val inflater = requireActivity().layoutInflater
        val isCheckedIndex = ArrayList<Int>()
        builder.setTitle("Name your Folder")
        val view = inflater.inflate(R.layout.diaolog_collection, null)
        builder.setView(view)
        val input = view.findViewById<EditText>(R.id.folder_name)

        builder.setMultiChoiceItems(
            folderNames.toTypedArray(),null,
            DialogInterface.OnMultiChoiceClickListener { dialog, index, isChecked ->
                //the dialog is interface, which is int, isChecked is boolean
                if (isChecked) {
                    isCheckedIndex.add(index)
                    Toast.makeText(context, "You Choose The ${folderNames.toTypedArray()[index]}",Toast.LENGTH_SHORT).show()

                } else if (isCheckedIndex.contains(index)) {
                    isCheckedIndex.remove(index)
                    Toast.makeText(context, "You Cancel The ${folderNames.toTypedArray()[index]}",Toast.LENGTH_SHORT).show()
                }
            })

        builder.setPositiveButton("SAVE",
            DialogInterface.OnClickListener { dialog, which ->
                run {
                    val title = input.text.toString()
                    Toast.makeText(context, "New Created $input Folder", Toast.LENGTH_SHORT).show()
                    viewModel.onClickCollection(title, post.id, post.url.toString())
                    viewModel.doneCollection(post.id)
                }
            })
        builder.setNegativeButton("CANCEL"
        ) { _, _ ->
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
}