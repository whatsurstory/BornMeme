package com.beva.bornmeme


import android.animation.Animator
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorFilter
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var binding: FragmentSplashBinding
    private lateinit var viewModel: SplashViewModel

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
        viewModel = SplashViewModel()
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
                binding.policyBtn.visibility = View.VISIBLE
                binding.policyBtn.setOnClickListener {
                    val url= "https://www.privacypolicies.com/live/679fc734-40b3-47ef-bcca-2c0e5a46483d"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
                //loading our custom made animations
                val ani  = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                //starting the animation
                binding.greetingText.startAnimation(ani)
                binding.gSignInBtn.startAnimation(ani)
//                showAgreeDialog()
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

        viewModel.leave.observe(
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
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    //"logintimes" to 1,
    //拿到上一次相同id的 logintimes 數字 -> n = User.logintimes
    private fun queryUserByUid(user: FirebaseUser) {

        val ref = Firebase.firestore.collection("Users")
            .document(user.uid)

        Firebase.firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
//            val loginTimes = n + 1
//            Timber.d("snapshot ??? $snapshot")
            if (snapshot.data != null) {
                Timber.d("ID: ${snapshot.id} snapshot.data != null")
                val checkUser = snapshot.toObject(User::class.java)
                UserManager.user = checkUser!!
                //when the member come back
//                Snackbar.make(requireView(), "Nice to See you Again! ${UserManager.user.userName}",
//                    Snackbar.LENGTH_SHORT)
//                    .setAction("Action", null).show()
//                Timber.d("UserManager ${UserManager.user}")
//                transaction.update(ref,FieldValue.arrayUnion(loginTimes))
            } else {
                Timber.d("ID: ${snapshot.id} snapshot.data == null")

                showAgreeDialog()

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
                    emptyList(),
                    emptyList()
                )
                transaction.set(ref,userData)
                UserManager.user = userData
                //when new comer sign-in
//                Snackbar.make(requireView(), "WelCome to Join! ${UserManager.user.userName}",
//                    Snackbar.LENGTH_SHORT)
//                    .setAction("Action", null).show()
            }
        }.addOnSuccessListener {
            Timber.d("Success to adding $ref")
            viewModel.leave()
            findNavController().navigate(MobileNavigationDirections.navigateToHomeFragment())

        }.addOnFailureListener {
            Timber.d("ERROR ${it.message}")
        }
    }

    private fun showAgreeDialog() {

        val item = LayoutInflater.from(requireContext()).inflate(R.layout.service_text, null)
        // set up spanned string with url
        val spannableString = SpannableString("點擊 同意並繼續 即代表您同意並確認您已閱讀我們的服務條款與隱私政策，了解我們如何收集、使用與分享您的資料。")
        val url= "https://www.privacypolicies.com/live/679fc734-40b3-47ef-bcca-2c0e5a46483d"
        spannableString.setSpan(URLSpan(url), 25, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val textView = item.findViewById<TextView>(R.id.url_text)
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance() // enable clicking on url span

        AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setView(item)
            .setPositiveButton(R.string.agreeAndContinue) { _, _ ->
                Toast.makeText(context, R.string.agreeAndContinue, Toast.LENGTH_SHORT).show()
            }
            .show()
            .getButton(DialogInterface.BUTTON_POSITIVE)
            .setTextColor(Color.parseColor("#181A19"))

    }

}
