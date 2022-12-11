package com.beva.bornmeme.keyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.beva.bornmeme.MobileNavigationDirections
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.FragmentSettingBinding
import com.beva.bornmeme.model.UserManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.Executors

class SettingFragment: BottomSheetDialogFragment() {

    private lateinit var binding: FragmentSettingBinding

    private val register = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Timber.i("beva \n register running")
        if (result.resultCode == Activity.RESULT_OK) {
            val editor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
            val stickerDirPath = result.data?.data.toString()
            editor.putString("stickerDirPath", stickerDirPath)
            editor.putString("lastUpdateDate", Calendar.getInstance().time.toString())
            editor.putString("recentCache", "")
            editor.putString("compatCache", "")
            editor.apply()
            Timber.i("bmkb, stickerDirPath: $stickerDirPath")
            val toaster = Toaster(requireContext())
            val executor = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())

            executor.execute {
                val totalStickers =
                    StickerImporter(requireContext(), toaster).importStickers(
                        stickerDirPath
                    )
                handler.post {
                    toaster.toastOnState(
                        arrayOf(
                            getString(R.string.imported_020, totalStickers),
                            getString(R.string.imported_031, totalStickers),
                            getString(R.string.imported_032, totalStickers),
                            getString(R.string.imported_033, totalStickers),
                        )
                    )
                    editor.putInt("numStickersImported", totalStickers)
                    editor.apply()
                }
            }
        }
    }

//    override fun getTheme() = R.style.CustomBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        dialog?.let {
//            val sheet = it as BottomSheetDialog
//            sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
//        }
        binding = FragmentSettingBinding.inflate(layoutInflater)

        binding.settingOpenKeyboard.setOnClickListener {
            Timber.i("beva \n click settingOpenKeyboard")
            enableKeyboard(it)
        }

        binding.updateStickerPackInfoBtn.setOnClickListener {
            Timber.i("beva \n click updateStickerPackInfoBtn")
            chooseDir(it)
        }

        binding.policyButton.setOnClickListener {
//                        val intent = Intent(
//                            Intent.ACTION_VIEW,
//                            Uri.parse("https://www.privacypolicies.com/live/679fc734-40b3-47ef-bcca-2c0e5a46483d")
//                        )
//                        startActivity(intent)
        findNavController().navigate(MobileNavigationDirections.navigateToServiceFragment(UserManager.user.agreement))
        }

        return binding.root
    }

    private fun enableKeyboard(view: View) {
        Timber.i("beva \n enableKeyboard running")
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
        startActivity(intent)
    }

    private fun chooseDir(view: View) {
        Timber.i("beva \n chooseDir running")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        register.launch(intent)
    }
}