package com.beva.bornmeme.ui.detail.img


import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.parseColor
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.adapters.ViewBindingAdapter.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentImgDetailBinding
import com.beva.bornmeme.databinding.SnackBarCustomBinding
import com.beva.bornmeme.model.Post
import com.beva.bornmeme.model.UserManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*


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
                    .placeholder(R.drawable._50)
                    .error(R.drawable.dino)
            ).into(binding.imgDetailImage)
        binding.imgDetailDescription.text = post.resources[1].url?.trim()

        if (post.like?.isEmpty() == true) {
            Timber.d("Post Like ${post.like}")
            binding.beforeThumbupBtn.setBackgroundResource(R.drawable._heart)
        } else {
            for (item in post.like!!) {
                if (item == UserManager.user.userId) {
                    Timber.d("item $item")
                    binding.beforeThumbupBtn.setBackgroundResource(R.drawable.heart)
                } else {
                    binding.beforeThumbupBtn.setBackgroundResource(R.drawable._heart)
                }
            }
        }

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
                                Toast.makeText(context, "已經追蹤該作者", Toast.LENGTH_SHORT).show()
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
//                if (it.isEmpty()) {
//                    binding.noSeeText.visibility = View.VISIBLE
//                    binding.noSeeText.typeWrite(viewLifecycleOwner,
//                        "it's a little bit quiet in here ...",
//                        80L)
//                } else {
                    binding.noSeeText.visibility = View.GONE
                    Timber.d(("Observe comment cell : $it"))
                    adapter.submitList(it)
                    adapter.notifyDataSetChanged()
//                }
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
        val postRef = FirebaseFirestore.getInstance().collection("Posts").document(post.id)
        val userRef = FirebaseFirestore.getInstance().collection("Users").document(UserManager.user.userId!!)
        binding.beforeThumbupBtn.setOnClickListener {
                userRef.update("likeId", FieldValue.arrayUnion(post.id))
                postRef.update("like", FieldValue.arrayUnion(UserManager.user.userId))
                .addOnSuccessListener {
                    Timber.d("Success add like")
                    binding.beforeThumbupBtn.setBackgroundResource(R.drawable.heart)
//                    Snackbar.make(this.requireView(), "喜歡喜歡~~~", Snackbar.LENGTH_SHORT)
//                        .setAction("Action", null).show()
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

            popupMenu.menu.add(Menu.NONE, 0, 0, "檢舉圖片")
            popupMenu.menu.add(Menu.NONE, 1, 1, "封鎖用戶")
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    0 -> reportDialog()
                    1 -> add2Block()
                }
                false
            }

        } else {
            popupMenu.menu.add(Menu.NONE, 0, 0, "刪除照片")
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
        binding.imgDetailImage.setOnLongClickListener {
//            Timber.d(("index 0 => ${post.resources[0].url}"))
            longClick2edit (post)
            true
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun add2Block() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(com.beva.bornmeme.R.layout.dialog_custom_delete, null)
        builder.setView(view)

        val alertDialog: AlertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        val message = view.findViewById<TextView>(R.id.delete_message)
        message.text = "你確定要封鎖人家嗎...\n再也看不見的那種?"

        val okay = view.findViewById<Button>(R.id.okay_delete_btn)
        okay.setOnClickListener {
            //先把資料裝進local 再上傳到firebase
            UserManager.user.blockList += post.ownerId
            Firebase.firestore.collection("Users")
                .document(UserManager.user.userId!!)
                .update("blockList", UserManager.user.blockList)
                .addOnCompleteListener {
                    Timber.d("add to block ${post.ownerId}")
                    alertDialog.dismiss()
                    findNavController().navigateUp()
                }
            }
        val cancel = view.findViewById<Button>(R.id.cancel_button)
        cancel.setOnClickListener { alertDialog.dismiss() }
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
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
//Make you can't dismiss by default -> builder.setCancelable(false)
        // Get the layout inflater
        val inflater = requireActivity().layoutInflater
        val isCheckedIndex = ArrayList<Int>()
        val list = ArrayList<String>()

        builder.setTitle("幫收藏夾取名字(*‘ v`*)")
        val view = inflater.inflate(R.layout.diaolog_collection, null)
        builder.setView(view)
        val input = view.findViewById<EditText>(R.id.folder_name)


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

        builder.setPositiveButton("確定",
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
                        }
                    }
                }
            })
        builder.setNegativeButton("取消",
            DialogInterface.OnClickListener { _, _ ->
//            Timber.d("check the selected item -> list size:${list.size} int size: ${isCheckedIndex.size}")
            })

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            .setTextColor(parseColor("#181A19"))
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setTextColor(parseColor("#181A19"))

    }



    @SuppressLint("SetTextI18n")
    private fun showDialog() {
        //delete for short
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(com.beva.bornmeme.R.layout.dialog_custom_delete, null)
        builder.setView(view)

        val alertDialog: AlertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        val message = view.findViewById<TextView>(R.id.delete_message)
        message.text = "確定要刪除這麼棒的智慧結晶嗎?"

        val okay = view.findViewById<Button>(R.id.okay_delete_btn)
        okay.setOnClickListener {
            val user = Firebase.firestore.collection("Users").document(post.ownerId)
                user.update("postQuantity", FieldValue.arrayRemove(post.id))
            val postId = FirebaseFirestore.getInstance().collection("Posts").document(post.id)
                postId.delete().addOnSuccessListener {
                    Timber.d("DocumentSnapshot successfully deleted${post.id}!")
                    alertDialog.dismiss()
                    findNavController().navigateUp()
                }
                .addOnFailureListener { e -> Timber.w("Error deleting document", e) }
        }

        val cancel = view.findViewById<Button>(com.beva.bornmeme.R.id.cancel_button)
        cancel.setOnClickListener { alertDialog.dismiss() }
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

        Toast.makeText(context, "下載完成",Toast.LENGTH_SHORT).show()
    }

//    private fun TextView.typeWrite(lifecycleOwner: LifecycleOwner, text: String, intervalMs: Long) {
//        this@typeWrite.text = ""
//        lifecycleOwner.lifecycleScope.launch {
//            repeat(text.length) {
//                delay(intervalMs)
//                this@typeWrite.text = text.take(it + 1)
//            }
//        }
//    }


    private fun longClick2edit (img: Post) {
        val builder = AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_image, null)
        builder.setView(view)
        val image = view.findViewById<ImageView>(R.id.gallery_img)
        Glide.with(image).load(post.resources[0].url).placeholder(R.drawable._50).into(image)

        builder.setMessage("就決定是${post.title}了嗎?(・∀・)つ⑩")
        builder.setPositiveButton("對沒錯") { dialog, _ ->
            val bitmapDrawable = image.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            saveImage(bitmap, img.id)
            Timber.d("filePath -> $bitmap")
        }
        builder.setNegativeButton("再想想", DialogInterface.OnClickListener { dialog, which ->

        })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(parseColor("#181A19"))
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(parseColor("#181A19"))
    }

    private fun saveImage(image: Bitmap, id:String): String? {
        var savedImagePath: String? = null
        val imageFileName = "$id.jpg"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/BornMeme"
        )
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Add the image to the system gallery
            galleryAddPic(savedImagePath)
        }
        return savedImagePath
    }
    private fun galleryAddPic(imagePath: String) {
        val file = File(imagePath)
        val newUri =
            FileProvider.getUriForFile(
                requireContext(),
                "com.beva.bornmeme.fileProvider", file
            )
        Timber.d("fileUri -> $newUri")
        findNavController().navigate(MobileNavigationDirections.navigateToEditFragment(newUri))
    }

    @SuppressLint("ResourceAsColor")
    private fun reportDialog() {
        val data = arrayOf("色情","暴力","賭博","非法交易","種族歧視")
//        val selected = booleanArrayOf(false,false,false,false,false)

        val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
        builder.setTitle("請選擇檢舉原因")
        builder.setMultiChoiceItems(data, null) { dialog, i , b ->
            val currentItem = data[i]
        }
        builder.setPositiveButton("確定") { dialogInterface, j ->
//            for (i in data.indices) if (selected[i]) {
//                selected[i] = false
//            }
            val customSnack= Snackbar.make(requireView(),"",Snackbar.LENGTH_INDEFINITE)
            val layout = customSnack.view as Snackbar.SnackbarLayout
            val bind = SnackBarCustomBinding.inflate(layoutInflater)
            bind.notToBlockBtn.setOnClickListener {
                customSnack.dismiss()
            }
            bind.toBlockBtn.setOnClickListener {
                UserManager.user.blockList += post.ownerId
            Firebase.firestore.collection("Users")
                .document(UserManager.user.userId!!)
                .update("blockList", UserManager.user.blockList)
                .addOnCompleteListener {
                    Timber.d("add to block ${post.ownerId}")
                    customSnack.dismiss()
                    findNavController().navigateUp()
                }
            }
            layout.addView(bind.root)
            customSnack.setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.white))
            customSnack.view.layoutParams = (customSnack.view.layoutParams as FrameLayout.LayoutParams)
                .apply {
                gravity = Gravity.TOP
            }

            layout.setPadding(0,0,0,0)
            customSnack.show()
        }
        builder.setNegativeButton("取消") { dialog, i ->

        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(parseColor("#181A19"))
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(parseColor("#181A19"))

    }

}

