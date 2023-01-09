package com.beva.bornmeme


import android.animation.Animator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.ColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.beva.bornmeme.databinding.FragmentSplashBinding
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber


class SplashFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var binding: FragmentSplashBinding
    private lateinit var viewModel: SplashViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(layoutInflater)
        viewModel = SplashViewModel()
        firebaseAuth = FirebaseAuth.getInstance()
        val googleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient =
            GoogleSignIn.getClient(this.requireActivity(), googleSignInOptions)

        binding.screenLottieView
            .addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    binding.googleSignInButton.visibility = View.VISIBLE
                    binding.greetingText.visibility = View.VISIBLE

                    //loading our custom made animations
                    val logInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                    //starting the animation
                    binding.greetingText.startAnimation(logInAnimation)
                    binding.googleSignInButton.startAnimation(logInAnimation)
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })

        //Button to Login
        binding.googleSignInButton.setOnClickListener {

            binding.loginLoadingAnimation.changeLayersColor(R.color.green)
            binding.loginLoadingAnimation.visibility = View.VISIBLE
            binding.loginLoadingAnimation.setAnimation(R.raw.bouncing_balls)
            signInGoogle()
        }

        viewModel.leaveSplash.observe(
            viewLifecycleOwner,
            Observer {
                it?.let {
                    viewModel.onLeaveCompleted()
                }
            }
        )

        return binding.root
    }

    //change lotties color
    private fun LottieAnimationView.changeLayersColor(
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
                val signInAccountTask =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(signInAccountTask)
            }
        }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val signInAccount: GoogleSignInAccount? = task.result
            if (signInAccount != null) {
                updateUI(signInAccount)
            }
        } else {
            Timber.d("handleResults task ERROR ${task.exception}")
            Toast.makeText(this.requireContext(), task.exception.toString(), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val authCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(authCredential)
            .addOnCompleteListener { authResult ->
                if (authResult.isSuccessful) {
                    UserManager.user.userId = authResult.result.user?.uid
                    authResult.result.user?.let {
                        queryUserByUid(it)
                    }

                } else {
                    Timber.d("updateUI task ERROR ${authResult.exception}")
                    Toast.makeText(context, authResult.exception.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun queryUserByUid(
        user: FirebaseUser) {
        var hasData = false
        val ref =
            Firebase.firestore
                .collection(getString(R.string.user_collection_text))
                .document(user.uid)

        Firebase.firestore.runTransaction { transaction ->
            val snapshot = ref.let { transaction.get(it) }
//            val loginTimes = n + 1
            if (snapshot.data != null) {
                val checkUser = snapshot.toObject(User::class.java)
                if (checkUser != null) {
                    UserManager.user = checkUser
                    hasData = checkUser.agreement
                }
//                transaction.update(ref,FieldValue.arrayUnion(loginTimes))
            } else {
                val userData = User(
                    userId = user.uid,
                    profilePhoto = user.photoUrl.toString(),
                    userName = user.displayName.toString(),
                    introduce = getString(R.string.introduce_text),
                    email = user.email.toString(),
                    registerTime = Timestamp.now(),
                    agreement = false
                )
                ref.let { transaction.set(it, userData) }
                UserManager.user = userData
                hasData = userData.agreement
            }
        }.addOnSuccessListener {
            getRegisterInfo(hasData)
        }.addOnFailureListener {
            Timber.d("ERROR ${it.message}")
            Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getRegisterInfo(hasData: Boolean) {
        if (hasData) {
            viewModel.leave()
            NavHostFragment.findNavController(this)
                .navigate(MobileNavigationDirections.navigateToHomeFragment())
        } else {
            showAgreeDialog(hasData)
        }
    }

    private fun showAgreeDialog(hasData:Boolean) {

        AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setMessage(getString(R.string.welcome_to_use_text))
//            .setView(item)
            .setPositiveButton(R.string.continue_text) { _, _ ->
                viewModel.leave()
//                findNavController(fragment).navigate(MobileNavigationDirections.navigateToHomeFragment())
                NavHostFragment.findNavController(this)
                    .navigate(MobileNavigationDirections.navigateToServiceFragment(hasData))
            }
            .show()
            .getButton(DialogInterface.BUTTON_POSITIVE)
            .setTextColor(requireContext().getColor(R.color.button_balck))
    }
}
