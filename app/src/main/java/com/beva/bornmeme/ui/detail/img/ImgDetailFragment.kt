package com.beva.bornmeme.ui.detail.img


import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.parseColor
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MainViewModel
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentImgDetailBinding
import com.beva.bornmeme.databinding.SnackBarCustomBinding
import com.beva.bornmeme.loadImage
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
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
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
                    .placeholder(R.drawable.place_holder)
            ).into(binding.imgDetailImage)
        binding.imgDetailDescription.text = post.resources[1].url?.trim()

        val postRef = FirebaseFirestore.getInstance()
            .collection("Posts").document(post.id)
        val userRef = FirebaseFirestore.getInstance()
            .collection("Users").document(UserManager.user.userId!!)

        if (post.like.isNullOrEmpty()) {
            binding.beforeThumbupBtn.setOnClickListener {
                userRef.update("likeId", FieldValue.arrayUnion(post.id))
                postRef.update("like", FieldValue.arrayUnion(UserManager.user.userId))
                binding.beforeThumbupBtn.setBackgroundResource(R.drawable.heart)
            }
        } else {
            //not null or empty
            for (item in post.like!!) {
                if (item == UserManager.user.userId) {
                    //item = User
                    binding.beforeThumbupBtn.setBackgroundResource(R.drawable.heart)
                    binding.beforeThumbupBtn.setOnClickListener {
                        userRef.update("likeId", FieldValue.arrayRemove(post.id))
                        postRef.update("like", FieldValue.arrayRemove(UserManager.user.userId))
                        binding.beforeThumbupBtn.setBackgroundResource(R.drawable._heart)
                    }
                } else {
                    //item != User
                    binding.beforeThumbupBtn.setOnClickListener {
                        userRef.update("likeId", FieldValue.arrayUnion(post.id))
                        postRef.update("like", FieldValue.arrayUnion(UserManager.user.userId))
                        binding.beforeThumbupBtn.setBackgroundResource(R.drawable.heart)
                    }
                }
            }
        }

        if (post.ownerId == UserManager.user.userId) {
            binding.followBtn.visibility = View.GONE
        } else {
            binding.followBtn.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ImgDetailViewModel(post.ownerId, requireContext())
        viewModel.getComments(post.id, requireContext())

        viewModel.userData.observe(viewLifecycleOwner, Observer { user ->
            binding.imgDetailUserImg.loadImage(user[0].profilePhoto)
            binding.imgDetailUserName.text = user[0].userName
            val db = Firebase.firestore.collection("Users")
            if (user[0].followers.isNullOrEmpty()) {
                Timber.d("adding")
                binding.followBtn.text = "Follow"
                binding.followBtn.setOnClickListener {
                    db.document(UserManager.user.userId!!)
                        .update("followList", FieldValue.arrayUnion(user[0].userId))
                    db.document(user[0].userId!!)
                        .update("followers", FieldValue.arrayUnion(UserManager.user.userId))
                }
            } else {
                //followers != null or empty
                for (item in user[0].followers) {
                    if (item == UserManager.user.userId) {
                        binding.followBtn.text = "Following"
                        binding.followBtn.setOnClickListener {
                            Timber.d("removing")
                            db.document(UserManager.user.userId!!)
                                .update("followList", FieldValue.arrayRemove(user[0].userId))
                            db.document(user[0].userId!!).update(
                                "followers",
                                FieldValue.arrayRemove(UserManager.user.userId)
                            )
                            binding.followBtn.text = "Follow"
                        }
                    } else {
                        binding.followBtn.text = "Follow"
                        binding.followBtn.setOnClickListener {
                            Timber.d("second adding")
                            db.document(UserManager.user.userId!!)
                                .update("followList", FieldValue.arrayUnion(user[0].userId))
                            db.document(user[0].userId!!)
                                .update("followers", FieldValue.arrayUnion(UserManager.user.userId))
                        }
                    }
                }
            }

        })


        binding.imgDetailTitle.text = post.title
        binding.imgDetailImage.loadImage(post.url)
        binding.imgDetailDescription.text = post.resources[1].url

        val adapter = CommentAdapter(viewModel.uiState, viewModel, this, requireContext(), inflater)
        binding.commentsRecycler.adapter = adapter

        //Observe the view of comments recycler
        viewModel.commentCells.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.noSeeText.visibility = View.GONE
                Timber.d(("Observe comment cell : $it"))
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })

        //Query All Comments
        viewModel.liveData.observe(viewLifecycleOwner) {
            it?.let {
                Timber.d(("Observe liveData : $it"))
                viewModel.initCells(it.filterBlock())
            }
        }

        //the button to see user information
        binding.imgDetailUserImg.setOnClickListener {
            findNavController().navigate(
                MobileNavigationDirections
                    .navigateToUserDetailFragment(post.ownerId)
            )
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
                showFolderDialog(it)
            }
        })
        //the button to take post to collection
        binding.collectionBtn.setOnClickListener {
            viewModel.getFolder(requireContext())
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
            checkSharePermission()
        }

        binding.templateBtn.setOnClickListener {
            checkTemplatePermission()
        }
        return binding.root
    }

    private fun checkTemplatePermission() {
        Dexter.withContext(requireContext()).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(
            object : PermissionListener {
                override fun onPermissionGranted(
                    p0: PermissionGrantedResponse?
                ) {
                    click2edit(post)
                }

                override fun onPermissionDenied(
                    p0: PermissionDeniedResponse?
                ) {
                    Toast.makeText(
                        context,
                        "拒絕存取相簿權限",
                        Toast.LENGTH_SHORT
                    ).show()
                    showRotationDialogForPermission()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?, p1: PermissionToken?
                ) {
                    showRotationDialogForPermission()
                }
            }).onSameThread().check()
    }

    private fun checkSharePermission() {
        Dexter.withContext(requireContext()).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(
            object : PermissionListener {
                override fun onPermissionGranted(
                    p0: PermissionGrantedResponse?
                ) {
                    getUri()
                }

                override fun onPermissionDenied(
                    p0: PermissionDeniedResponse?
                ) {
                    Toast.makeText(
                        context,
                        "拒絕存取相簿權限",
                        Toast.LENGTH_SHORT
                    ).show()
                    showRotationDialogForPermission()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?, p1: PermissionToken?
                ) {
                    showRotationDialogForPermission()
                }
            }).onSameThread().check()
    }

    private fun showRotationDialogForPermission() {
        AlertDialog.Builder(requireContext())
            .setMessage("看起來你還沒有打開權限 \n 打開之後即可完整使用功能哦!")

            .setPositiveButton("前往設定") { _, _ ->

                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", activity?.packageName, null)
                    intent.data = uri
                    startActivity(intent)

                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }

            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }.show()
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

    private fun getUri() {
        val uri =
            FirebaseStorage.getInstance().reference.child("img_edited/" + post.id + ".jpg")
        val filePath = requireContext().filesDir.absolutePath + "/" + post.id + ".jpg"
//        Timber.d("filePath -> $filePath")

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
                    intent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    startActivity(Intent.createChooser(intent, "Share Image"))
//                        startActivity(intent)
                }
            }
    }

    @SuppressLint("ResourceAsColor")
    private fun showFolderDialog(folderNames: List<String>) {
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
                    list.add(folderNames.toTypedArray()[index])

                } else if (isCheckedIndex.contains(index)) {
                    isCheckedIndex.remove(index)
                    list.remove(folderNames.toTypedArray()[index])

                }
            })

        builder.setPositiveButton(getString(R.string.sure_text),
            DialogInterface.OnClickListener { dialog, which ->
                run {
                    var title: String
                    if (list.isNotEmpty() && input.text.toString().isEmpty()) {
                        for (i in 0 until list.size) {
                            title = list[i]
//                            Timber.d("title $title")
                            viewModel.onClickCollection(title, post.id, post.url.toString())
                            viewModel.doneCollection(post.id, requireContext())
//                        Toast.makeText(context, "New Created $input Folder", Toast.LENGTH_SHORT).show()
                        }
                    } else if (list.isEmpty() && input.text.toString().isNotEmpty()) {
                        title = input.text.toString()
                        viewModel.onClickCollection(title, post.id, post.url.toString())
                        viewModel.doneCollection(post.id, requireContext())

                    } else if (list.isNotEmpty() && input.text.toString().isNotEmpty()) {
                        list.add(input.text.toString())
                        for (i in 0 until list.size) {
                            title = list[i]
//                            Timber.d("title $title")
                            viewModel.onClickCollection(title, post.id, post.url.toString())
                            viewModel.doneCollection(post.id, requireContext())
                        }
                    }
                }
            })
        builder.setNegativeButton(getString(R.string.cancel_text),
            DialogInterface.OnClickListener { _, _ ->
//            Timber.d("check the selected item -> list size:${list.size} int size: ${isCheckedIndex.size}")
            })

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            .setTextColor(requireContext().getColor(R.color.button_balck))
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setTextColor(requireContext().getColor(R.color.button_balck))
    }


    @SuppressLint("SetTextI18n")
    private fun showDialog() {
        //delete for short
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_custom_delete, null)
        builder.setView(view)

        val alertDialog: AlertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        val message = view.findViewById<TextView>(R.id.delete_message)
        message.text = "確定要刪除這麼棒的智慧結晶嗎?"

        val okay = view.findViewById<Button>(R.id.okay_delete_btn)
        okay.setOnClickListener {
            val user = Firebase.firestore.collection(getString(R.string.user_collection_text))
                .document(post.ownerId)
            user.update("postQuantity", FieldValue.arrayRemove(post.id))
            val postId =
                FirebaseFirestore.getInstance().collection(getString(R.string.post_collection_text))
                    .document(post.id)
            postId.delete().addOnSuccessListener {
                Timber.d("DocumentSnapshot successfully deleted${post.id}!")
                alertDialog.dismiss()
                findNavController().navigateUp()
            }
                .addOnFailureListener { e -> Timber.w("Error deleting document", e) }
        }

        val cancel = view.findViewById<Button>(R.id.cancel_button)
        cancel.setOnClickListener { alertDialog.dismiss() }
    }


    private fun click2edit(img: Post) {
        val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_image, null)
        builder.setView(view)
        val image = view.findViewById<ImageView>(R.id.gallery_img)
        image.loadImage(post.resources[0].url)

        builder.setMessage("就決定是${post.title}了嗎?(・∀・)つ⑩")
        builder.setPositiveButton(getString(R.string.yes_btn)) { dialog, _ ->
            val bitmapDrawable = image.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            saveImage(bitmap, img.id)
//            Timber.d("filePath -> $bitmap")
        }
        builder.setNegativeButton(
            getString(R.string.no_btn),
            DialogInterface.OnClickListener { dialog, which ->

            })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            .setTextColor(requireContext().getColor(R.color.button_balck))
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setTextColor(requireContext().getColor(R.color.button_balck))
    }

    private fun saveImage(image: Bitmap, id: String): String? {
        var savedImagePath: String? = null
        val imageFileName = "$id.jpg"
        val storageDir = File(
            context?.filesDir,
            System.currentTimeMillis().toString() + ".jpg"
        )
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val outputStream: OutputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
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
//        Timber.d("fileUri -> $newUri")
        val mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        mainViewModel.editingImg = newUri
        findNavController().navigate(MobileNavigationDirections.navigateToEditFragment(newUri))
    }

    @SuppressLint("ResourceAsColor")
    private fun reportDialog() {
        val data = arrayOf("色情", "暴力", "賭博", "非法交易", "種族歧視")

        val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
        builder.setTitle("請選擇檢舉原因")
        builder.setMultiChoiceItems(data, null) { dialog, i, b ->
            val currentItem = data[i]
        }
        builder.setPositiveButton(getString(R.string.sure_text)) { dialogInterface, j ->
//            for (i in data.indices) if (selected[i]) {
//                selected[i] = false
//            }
            val customSnack = Snackbar.make(requireView(), "", Snackbar.LENGTH_INDEFINITE)
            val layout = customSnack.view as Snackbar.SnackbarLayout
            val bind = SnackBarCustomBinding.inflate(layoutInflater)
            bind.notToBlockBtn.setOnClickListener {
                customSnack.dismiss()
            }
            bind.toBlockBtn.setOnClickListener {
                UserManager.user.blockList += post.ownerId
                UserManager.user.userId?.let { id ->
                    Firebase.firestore.collection(getString(R.string.user_collection_text))
                        .document(id)
                        .update("blockList", UserManager.user.blockList)
                        .addOnCompleteListener {
//                            Timber.d("add to block ${post.ownerId}")
                            customSnack.dismiss()
                            findNavController().navigateUp()
                        }
                }
            }
            layout.addView(bind.root, 0)
            customSnack.setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.white
                )
            )
            customSnack.view.layoutParams =
                (customSnack.view.layoutParams as FrameLayout.LayoutParams)
                    .apply {
                        gravity = Gravity.TOP
                    }
            customSnack.show()
        }
        builder.setNegativeButton(getString(R.string.cancel_text)) { dialog, i ->
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            .setTextColor(requireContext().getColor(R.color.button_balck))
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            .setTextColor(requireContext().getColor(R.color.button_balck))
    }
}

//Todo: button https://github.com/givemepassxd999/alert_dialog_demo/blob/master/app/src/main/java/com/example/givemepss/alertdailogdemo/MainActivity.kt