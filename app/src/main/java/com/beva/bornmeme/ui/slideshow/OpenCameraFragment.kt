package com.beva.bornmeme.ui.slideshow


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.databinding.FragmentCameraBinding


class OpenCameraFragment : Fragment() {

    private lateinit var binding: FragmentCameraBinding
    private lateinit var uri: Uri
    private lateinit var bgBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            uri = bundle.getParcelable("uri")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this).get(OpenCameraViewModel::class.java)
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        val photo = binding.originPhoto
        val upperText = binding.upperText
        val bottomText = binding.bottomText

        binding.previewBtn.setOnClickListener {
            //transfer to bitmap
            upperText.toString()
            bottomText.toString()
            upperText.buildDrawingCache()
            bottomText.buildDrawingCache()
            bgBitmap = BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(uri))
            photo.setImageBitmap(bgBitmap)
            photo.setImageBitmap(binding.upperText.drawingCache)
            photo.setImageBitmap(binding.bottomText.drawingCache)
            binding.originPhoto.setImageBitmap(mergeBitmap(bgBitmap, binding.upperText.drawingCache,binding.bottomText.drawingCache))
//            mergeBitmap(bgBitmap, binding.upperText.drawingCache,binding.bottomText.drawingCache)
        }

        binding.publishBtn.setOnClickListener {
            it?.let {
                findNavController().navigate(
                    MobileNavigationDirections.navigateToHomeFragment())
//                        mergeBitmap(
//                            bgBitmap,
//                            binding.upperText.drawingCache,
//                            binding.bottomText.drawingCache
//                )
            }
        }

        return binding.root
    }

    private fun mergeBitmap(
        firstImage: Bitmap,
        secondImage: Bitmap,
        thirdImage: Bitmap
    ): Bitmap {
        val result = Bitmap.createBitmap(firstImage.width, firstImage.height, firstImage.config)
        val canvas = Canvas(result)
        canvas.drawBitmap(firstImage, 0f, 0f, null)
        val secondImageLeft = (firstImage.width - secondImage.width).toFloat() / 2
        canvas.drawBitmap(secondImage, secondImageLeft, 0f, null)
        val thirdImageLeft = (firstImage.width - thirdImage.width).toFloat() / 2
        val thirdImageTop = (firstImage.height - thirdImage.height).toFloat()
        canvas.drawBitmap(thirdImage, thirdImageLeft, thirdImageTop, null)

        //圖片中心點放置座標(resource, left, top,(may be null)): (背景寬 - 內容寬) / 2
        //**require parameter type is float
//        Log.d("secondImageLeft","secondImageLeft = $secondImageLeft")
//        Log.d("thirdImageLeft","thirdImageLeft = $thirdImageLeft")
//        Log.d("thirdImageTop","thirdImageTop = $thirdImageTop")
        findNavController().navigate(MobileNavigationDirections.navigateToGalleryDialog(result))
        return result
    }

    //Put the bitmap into local gallery
//    private fun merge(view: View) {
//        val contentValues = ContentValues()
//        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis())
//        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//        val uri =
//            activity?.contentResolver?.insert(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                contentValues
//            )
//        Log.d("uri","$uri")
//        Log.d("contentValues" , "$contentValues")
//        uri?.apply {
//            val ops = activity?.contentResolver?.openOutputStream(this)
//            mergeBitmap(
//                bgBitmap,
//                binding.upperText.drawingCache,
//                binding.bottomText.drawingCache
//            )?.compress(Bitmap.CompressFormat.JPEG, 100, ops)
//            ops?.close()
//        }
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Fixed the Photo too small
        //TODO: Fixed the Text Size not same
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        BitmapFactory.decodeStream(
            requireContext().contentResolver.openInputStream(uri),
            null,
            options)

        //Gallery Image origin Width &Height
        val oriWidth = options.outWidth
        val oriHeight = options.outHeight
//        Log.w("oriWidth", "oriWidth=${oriWidth}")
//        Log.w("oriHeight", "oriHeight=${oriHeight}")

        //Screen ViewWidth & Height
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
//        Log.i("screenWidth", "screenWidth = $screenWidth")
//        Log.i("screenHeight", "screenHeight = $screenHeight")

        //After combine Image Height
        //**require number type is float but the return number (after operating) is Int
        //螢幕寬 除以 圖片寬 乘以 圖片高 = 符合畫面比例高
        val height = (screenWidth.toFloat() / oriWidth.toFloat() * oriHeight.toFloat()).toInt()
        //our image will fit the screen width
//        Log.d("final width", "width = $screenWidth")
//        Log.d("final height", "height = $height")

        val layoutParam = ConstraintLayout.LayoutParams(
            screenWidth,
            height
        )
        //let new height and width assign as constraint layout parameter
        binding.originPhoto.layoutParams = layoutParam
        binding.originPhoto.scaleType = ImageView.ScaleType.FIT_CENTER
        binding.originPhoto.setImageURI(uri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding
    }
}