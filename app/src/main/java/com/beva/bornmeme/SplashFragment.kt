package com.beva.bornmeme


import android.animation.Animator
import android.app.Activity
import android.graphics.ColorFilter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.beva.bornmeme.databinding.FragmentSplashBinding
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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

        binding.lottie.addAnimatorListener(object: Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }


            override fun onAnimationEnd(animation: Animator?) {
                binding.gSignInBtn.visibility = View.VISIBLE
                binding.greetingText.visibility = View.VISIBLE
                //loading our custom made animations
                val ani  = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                //starting the animation
                binding.greetingText.startAnimation(ani)
                binding.gSignInBtn.startAnimation(ani)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })

        //Button to Login
        binding.gSignInBtn.setOnClickListener {
            val color = this.context?.let {requireContext().getColor(R.color.green) }
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

    //change lotties color
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
                queryUserByUid(it.result.user!!)

            } else {
                Timber.d("task ERROR ${it.exception}")
                Toast.makeText(this.requireContext(), it.exception.toString(), Toast.LENGTH_SHORT)
                    .show()

            }
        }
    }

    //"logintimes" to 1,
    //拿到上一次相同id的 logintimes 數字 -> n = User.logintimes
    private fun queryUserByUid(user: FirebaseUser) {

        val ref = Firebase.firestore.collection("Users").document(user.uid)
        Firebase.firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
//            val loginTimes = n + 1
            Timber.d("snapshot ??? $snapshot")
            if (snapshot.data != null) {
                Timber.d("ID: ${snapshot.id} snapshot.data != null ${snapshot.data}")
                val checkUser = snapshot.toObject(User::class.java)
                UserManager.user = checkUser!!
                Timber.d("UserManager ${UserManager.user}")
//                transaction.update(ref,FieldValue.arrayUnion(loginTimes))
            } else {
                Timber.d("ID: ${snapshot.id} snapshot.data == null")
                val userData = User(
                    user.uid,
                    user.photoUrl.toString(),
                    user.displayName.toString(),
                    "",
                    "",
                    user.email.toString(),
                    Timestamp.now(),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyList()
                )
                transaction.set(ref,userData)
                UserManager.user = userData
            }
        }.addOnSuccessListener {
            Timber.d("Success to adding $ref")
            findNavController().navigate(MobileNavigationDirections.navigateToHomeFragment())

        }.addOnFailureListener {
            Timber.d("ERROR ${it.message}")
        }
    }

}
