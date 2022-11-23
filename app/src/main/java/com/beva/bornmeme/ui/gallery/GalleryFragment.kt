package com.beva.bornmeme.ui.gallery


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentGalleryBinding
import com.beva.bornmeme.model.Image
import com.bumptech.glide.Glide
import timber.log.Timber


class GalleryFragment: Fragment() {

    private lateinit var binding: FragmentGalleryBinding
    private lateinit var viewModel:GalleryViewModel
    private lateinit var adapter: GalleryAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGalleryBinding.inflate(layoutInflater)
        viewModel = GalleryViewModel()
        adapter = GalleryAdapter(
            GalleryAdapter.OnClickListener {
                showDialog(it)
            }
        )

        binding.verticalRecycle.layoutManager = GridLayoutManager(this.context, 3)

        binding.verticalRecycle.adapter = adapter

        viewModel.liveData.observe(viewLifecycleOwner, Observer{
            Timber.d("imageItems observe, $it")
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })

        return binding.root
    }

    private fun showDialog (img:Image) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_image, null)
        builder.setView(view)
        val image = view.findViewById<ImageView>(R.id.gallery_img)
        Glide.with(image).load(img.url).placeholder(R.drawable._50).into(image)

        builder.setTitle("Is ${img.title} your final choice?")
        builder.setPositiveButton("Yes") { dialog, _ ->
             Timber.d("img url -> ${img.url}")
        }
        builder.setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->

        })
        val alertDialog: AlertDialog = builder.create()
        val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        alertDialog.show()
//        alertDialog.window?.setLayout(, (4 * height)/5)
    }

}

