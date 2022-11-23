package com.beva.bornmeme.ui.detail.img


import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.databinding.FragmentImgDetailBinding
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.UserManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File


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
        binding.imgDetailTitle.text = post.title
        Glide.with(this)
            .load(post.url)
            .apply(
                RequestOptions()
                    .placeholder(com.beva.bornmeme.R.drawable._50)
                    .error(com.beva.bornmeme.R.drawable.dino)
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
                            .placeholder(com.beva.bornmeme.R.drawable._50)
                            .error(com.beva.bornmeme.R.drawable.dino)
                    ).into(binding.imgDetailUserImg)
                binding.imgDetailUserName.text = it[0].userName

                //follow button
                if (post.ownerId == UserManager.user.userId) {
                    binding.followBtn.visibility = View.GONE
                } else {
                    for (item in it[0].followers) {
                        if (item == UserManager.user.userId) {
                            binding.followBtn.text = "Following"
                            binding.followBtn.setOnClickListener {
                                Toast.makeText(context, "你已經追蹤該作者", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            binding.followBtn.text = "Follow"
                            //the button to follow other users
                            binding.followBtn.setOnClickListener {
                                viewModel.onClickToFollow(post.ownerId, binding)
                            }
                        }
                    }
                }
            }
        })


        binding.imgDetailTitle.text = post.title
        Glide.with(this)
            .load(post.url)
            .apply(
                RequestOptions()
                    .placeholder(com.beva.bornmeme.R.drawable._50)
                    .error(com.beva.bornmeme.R.drawable.dino)
            ).into(binding.imgDetailImage)
        binding.imgDetailDescription.text = post.resources[1].url

        val adapter = CommentAdapter(viewModel.uiState)
        binding.commentsRecycler.adapter = adapter

        //Observe the view of comments recycler
        viewModel.commentCells.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.isEmpty()) {
                    binding.noSeeText.visibility = View.VISIBLE
                    binding.noSeeText.typeWrite(viewLifecycleOwner,
                        "it's a little bit quiet in here ...",
                        80L)
                } else {
                    binding.noSeeText.visibility = View.GONE
                    Timber.d(("Observe comment cell : $it"))
                    adapter.submitList(it)
                    adapter.notifyDataSetChanged()
                }
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
            findNavController().navigate(
                MobileNavigationDirections
                    .navigateToUserDetailFragment(post.ownerId)
            )
        }
        //the button to like post
        binding.likeBtn.setOnClickListener {
            FirebaseFirestore.getInstance()
                .collection("Posts").document(post.id)
                .update("like", FieldValue.arrayUnion(UserManager.user.userId))
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
            findNavController().navigate(
                MobileNavigationDirections
                    .navigateToCommentDialog(postId = post.id)
            )
        }

        viewModel.navigate2Comment.observe(viewLifecycleOwner) {
            Timber.d("Observe navigate $it")
            it?.let {
                findNavController().navigate(
                    MobileNavigationDirections
                        .navigateToCommentDialog(
                            postId = it.comment.postId,
                            parentId = it.comment.commentId
                        )
                )
                viewModel.onDetailNavigated()
                Timber.d("navigate end")
            }
        }

        viewModel.folderData.observe(viewLifecycleOwner, Observer {
            it?.let {
                showAlert(it)
            }
        })
        //the button to take post to collection
        binding.collectionBtn.setOnClickListener {
            viewModel.getFolder()
        }

        //the menu button to report and other feature
        val popupMenu = PopupMenu(
            context,
            binding.reportBtn
        )
        if (post.ownerId != UserManager.user.userId) {
            popupMenu.menu.add(Menu.NONE, 0, 0, "Report the Image")
            popupMenu.menu.add(Menu.NONE, 1, 1, "Report the User")
            popupMenu.setOnMenuItemClickListener {
                when (val id = it.itemId) {
                    0 -> Toast.makeText(context, "ID $id -> Report the Image", Toast.LENGTH_SHORT)
                        .show()
                    1 -> Toast.makeText(context, "ID $id -> Report the User", Toast.LENGTH_SHORT)
                        .show()
                }
                false
            }
        } else {
            popupMenu.menu.add(Menu.NONE, 0, 0, "Delete the image")
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    0 -> showDialog()
                }
                false
            }
        }

        binding.reportBtn.setOnClickListener {
            popupMenu.show()
        }

        binding.shareBtn.setOnClickListener {
                if (SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {

                        val uri = FirebaseStorage.getInstance().reference.child("img_edited/" + post.id + ".jpg")
                        val filePath = requireContext().filesDir.absolutePath + "/" + post.id + ".jpg"
                        Timber.d("filePath -> $filePath")

                        uri.getFile(Uri.parse(filePath))
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Timber.d("success")
                                    //get Uri by file path
//                        Uri.parse("content:/$filePath")
//                        val fileUri = Uri.fromFile(File(filePath))
                                    val file = File(filePath)
                                    val newUri =
                                        FileProvider.getUriForFile(
                                            requireContext(),
                                            "com.beva.bornmeme.fileProvider", file
                                        )
                                    Timber.d("fileUri -> $newUri")
                                    val intent = Intent(Intent.ACTION_SEND)
                                    intent.type = "image/*"
                                    intent.putExtra(
                                        Intent.EXTRA_STREAM,
                                        newUri
                                    )
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//                                    startActivity(Intent.createChooser (intent, "Share Image"))
                                    startActivity(intent)
                                }
                            }
                } else {
                        requestPermission()
                }
            }

        }

        //the permission problem in samsung, maybe due to the version of android
        binding.downloadBtn.setOnClickListener {
            downLoad("${post.id}.jpg", "File Desc", post.url.toString())
        }

        //BUG: the argument can't be rendering on newView
//        binding.imgDetailImage.setOnClickListener {
//            Timber.d(("index 0 => ${post.resources[0].url}"))
//            val uri : Uri = (post.resources[0].url.toString()).toUri()
//            findNavController().navigate(MobileNavigationDirections.navigateToEditFragment(uri))
//        }
        return binding.root
    }

    private fun requestPermission() {

        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(
                    String.format(
                        "package:%s",
                        getApplicationContext<Context>().packageName
                    )
                )
                startActivityForResult(intent, 2296)


            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 2296)

            }
        } else {
            //below android 11
//            ActivityCompat.requestPermissions(
//                this.requireActivity(),
//                arrayOf(WRITE_EXTERNAL_STORAGE),
//                2296
//            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(
                        this.context,
                        "Allow permission for storage access!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun showAlert(folderNames: List<String>) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), com.beva.bornmeme.R.style.AlertDialogTheme)
//Make you can't dismiss by default -> builder.setCancelable(false)
        // Get the layout inflater
        val inflater = requireActivity().layoutInflater
        val isCheckedIndex = ArrayList<Int>()
        val list = ArrayList<String>()

        builder.setTitle("Name your Folder")
        val view = inflater.inflate(com.beva.bornmeme.R.layout.diaolog_collection, null)
        builder.setView(view)
        val input = view.findViewById<EditText>(com.beva.bornmeme.R.id.folder_name)


        builder.setMultiChoiceItems(
            folderNames.toTypedArray(), null,
            DialogInterface.OnMultiChoiceClickListener { dialog, index, isChecked ->
                //the dialog is interface, which is int, isChecked is boolean
                if (isChecked) {
                    isCheckedIndex.add(index)
//                    Toast.makeText(context, "You Choose The ${folderNames.toTypedArray()[index]}",Toast.LENGTH_SHORT).show()
                    list.add(folderNames.toTypedArray()[index])

//                    Timber.d("check index list $isCheckedIndex")
//                    Timber.d("check selected item $list")

                } else if (isCheckedIndex.contains(index)) {
                    isCheckedIndex.remove(index)
//                    Toast.makeText(context, "You Cancel The ${folderNames.toTypedArray()[index]}",Toast.LENGTH_SHORT).show()
                    list.remove(folderNames.toTypedArray()[index])

                }
            })

        builder.setPositiveButton("SAVE",
            DialogInterface.OnClickListener { dialog, which ->
                run {
                    var title: String
                    if (list.isNotEmpty() && input.text.toString().isEmpty()) {
                        for (i in 0 until list.size) {
                            title = list[i]
                            Timber.d("title $title")
                            viewModel.onClickCollection(title, post.id, post.url.toString())
                            viewModel.doneCollection(post.id)
//                        Toast.makeText(context, "New Created $input Folder", Toast.LENGTH_SHORT).show()
                        }
                    } else if (list.isEmpty() && input.text.toString().isNotEmpty()) {
                        title = input.text.toString()
                        viewModel.onClickCollection(title, post.id, post.url.toString())
                        viewModel.doneCollection(post.id)

                    } else if (list.isNotEmpty() && input.text.toString().isNotEmpty()) {
                        list.add(input.text.toString())
                        for (i in 0 until list.size) {
                            title = list[i]
                            Timber.d("title $title")
                            viewModel.onClickCollection(title, post.id, post.url.toString())
                            viewModel.doneCollection(post.id)
//                            Toast.makeText(context, "New Created $input Folder", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        builder.setNegativeButton("CANCEL",
            DialogInterface.OnClickListener { _, _ ->
//            Timber.d("check the selected item -> list size:${list.size} int size: ${isCheckedIndex.size}")
            })

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()

        val saveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        with(saveButton) {
            setBackgroundColor(com.beva.bornmeme.R.color.black)
            setPadding(0, 0, 20, 0)
            setTextColor(com.beva.bornmeme.R.color.light_blue)
        }

        val cancelButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        with(cancelButton) {
            setBackgroundColor(com.beva.bornmeme.R.color.black)
            setPadding(0, 0, 20, 0)
            setTextColor(com.beva.bornmeme.R.color.light_blue)
        }
    }

    private fun showDialog() {
        //delete for short
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Are You Sure to Delete ${post.id}?")
        builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
            FirebaseFirestore.getInstance()
                .collection("Posts")
                .document(post.id)
                .delete()
                .addOnSuccessListener {
                    Timber.d("DocumentSnapshot successfully deleted!")
                    findNavController().navigate(MobileNavigationDirections.navigateToHomeFragment())
                }
                .addOnFailureListener { e -> Timber.w("Error deleting document", e) }
        })
        builder.setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->

        })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }


    //need permission of write in
    private fun downLoad(fileName: String, desc: String, url: String) {
        val downloadManager = context?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            .setTitle(fileName)
            .setDescription(desc)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, fileName)
        downloadManager.enqueue(request)
        Snackbar.make(this.requireView(), "DownLoad Finished", Snackbar.LENGTH_SHORT)
            .setAction("Action", null).show()
    }

    private fun TextView.typeWrite(lifecycleOwner: LifecycleOwner, text: String, intervalMs: Long) {
        this@typeWrite.text = ""
        lifecycleOwner.lifecycleScope.launch {
            repeat(text.length) {
                delay(intervalMs)
                this@typeWrite.text = text.take(it + 1)
            }
        }
    }


}

