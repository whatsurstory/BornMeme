package com.beva.bornmeme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.beva.bornmeme.databinding.FragmentServicePolicyBinding
import com.beva.bornmeme.model.UserManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class ServiceFragment: Fragment() {

    lateinit var binding:FragmentServicePolicyBinding
    private var agreementKey: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            agreementKey = bundle.getBoolean("agreementKey")
        }
        binding = FragmentServicePolicyBinding.inflate(layoutInflater)


        if (agreementKey) {
            binding.checkBox.isChecked = true
            binding.checkBox.isEnabled = false
        } else {
            binding.checkBox.isChecked = false
            binding.checkBox.isEnabled = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

               val user = UserManager.user.userId?.let {
                    Firebase.firestore.collection("Users").document(it)
                }
                user?.update("agreement", true)?.addOnSuccessListener {
                    Timber.d("user agreement -> $user")
                    findNavController().navigate(MobileNavigationDirections.navigateToHomeFragment())
                }
            }
        }

        return binding.root
    }
}