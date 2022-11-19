package com.beva.bornmeme


import android.widget.TextView
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.ApiException
import android.app.Activity
import android.content.Intent
import android.graphics.ColorFilter
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.voice.VoiceInteractionSession
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.ColorRes
import androidx.collection.arrayMapOf
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.beva.bornmeme.databinding.FragmentSplashBinding
import com.beva.bornmeme.model.UserManager
import com.beva.bornmeme.model.UserManager.user
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.handleCoroutineException
import timber.log.Timber

class SplashFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var binding: FragmentSplashBinding

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)

//        Handler(Looper.getMainLooper()).postDelayed({
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }, 1000)

//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this.requireActivity(), gso)

        //Button to Login
        binding.gSignInBtn.setOnClickListener {
            val color = this.context?.let {requireContext().getColor(R.color.white) }
            val filter = SimpleColorFilter(color!!)
            val keyPath = KeyPath("**")
            val callback: LottieValueCallback<ColorFilter> = LottieValueCallback(filter)

            binding.loginLoading.addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback)
            binding.loginLoading.visibility = View.VISIBLE
            binding.loginLoading.setAnimation(R.raw.bouncing_balls)
            signInGoogle()
        }

        return binding.root
    }

    fun LottieAnimationView.changeLayersColor(
        @ColorRes colorRes: Int
    ) {
        val color = ContextCompat.getColor(context, colorRes)
        val filter = SimpleColorFilter(color)
        val keyPath = KeyPath("**")
        val callback: LottieValueCallback<ColorFilter> = LottieValueCallback(filter)

        addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback)
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            }
        }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            }
        } else {
            Timber.d("task ERROR ${task.exception}")
            Toast.makeText(this.requireContext(), task.exception.toString(), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                UserManager.user.userId = it.result.user?.uid
                Timber.d("check User -> ${UserManager.user.userId}")
                saveUidData(it.result.user!!)
                val intent = Intent(this.requireContext() , MainActivity::class.java)
                startActivity(intent)
            } else {
                Timber.d("task ERROR ${it.exception}")
                Toast.makeText(this.requireContext(), it.exception.toString(), Toast.LENGTH_SHORT)
                    .show()

            }
        }
    }

    //"logintimes" to 1,
    //拿到上一次相同id的 logintimes 數字 -> n = User.logintimes
    private fun saveUidData(user: FirebaseUser) {

        val ref = Firebase.firestore.collection("Users").document(user.uid)
        Firebase.firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
//            val loginTimes = n + 1
            Timber.d("snapshot ??? $snapshot")
            if (snapshot.data != null) {
                Timber.d("ID: ${snapshot.id} snapshot.data != null ${snapshot.data}")
//                transaction.update(ref,FieldValue.arrayUnion(loginTimes))
            } else {
                Timber.d("ID: ${snapshot.id} snapshot.data == null")
                transaction.set(
                    ref, hashMapOf(
                        "userId" to user.uid,
                        "profilePhoto" to user.photoUrl,
                        "userName" to user.displayName,
                        "registerTime" to Timestamp.now(),
                        "email" to user.email,
                        "introduce" to null,
                        "postQuantity" to null,
                        "followers" to null,
                        "followList" to null,
                        "commentsId" to null,
                        "collection" to null,
                        "blockList" to null
                    )
                )
            }
        }.addOnSuccessListener {
            Timber.d("Success to adding $ref")

        }.addOnFailureListener {
            Timber.d("ERROR ${it.message}")
        }
    }

}
