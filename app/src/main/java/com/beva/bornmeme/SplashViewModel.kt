package com.beva.bornmeme

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.beva.bornmeme.model.User
import com.beva.bornmeme.model.UserManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class SplashViewModel : ViewModel() {

    // Handle leave
    private val _leaveSplash = MutableLiveData<Boolean>()

    val leaveSplash: LiveData<Boolean>
        get() = _leaveSplash

    private fun leave() {
        _leaveSplash.value = true
    }

    fun onLeaveCompleted() {
        _leaveSplash.value = null
    }

    fun queryUserByUid(user: FirebaseUser,
                       application: Application?,
                       fragment: SplashFragment) {
        var hasData = false
        val ref = application?.let {
            Firebase.firestore
                .collection(it.getString(R.string.user_collection_text))
                .document(user.uid)
        }

        Firebase.firestore.runTransaction { transaction ->
            val snapshot = ref?.let { transaction.get(it) }
//            val loginTimes = n + 1
            if (snapshot?.data != null) {
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
                    email = user.email.toString(),
                    registerTime = Timestamp.now(),
                    agreement = false
                )
                ref?.let { transaction.set(it, userData) }
                UserManager.user = userData
                hasData = userData.agreement
            }
        }.addOnSuccessListener {
            getRegisterInfo(hasData, fragment, application)

        }.addOnFailureListener {
            Timber.d("ERROR ${it.message}")
            Toast.makeText(application, it.message.toString(), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getRegisterInfo(hasData: Boolean, fragment: SplashFragment,application: Application?) {
        if (hasData) {
            leave()
            findNavController(fragment)
                .navigate(MobileNavigationDirections.navigateToHomeFragment())
        } else {
            showAgreeDialog(application, fragment, hasData)
        }
    }

    private fun showAgreeDialog(application: Application?, fragment: SplashFragment, hasData:Boolean) {

//        val item = LayoutInflater.from(context)
//            .inflate(R.layout.service_text, null)
//
//        // set up spanned string with url
//        val spannableString =
//            SpannableString(context.getString(R.string.service_text_with_url))
//
//        spannableString
//            .setSpan(
//                URLSpan("https://www.privacypolicies.com/live/679fc734-40b3-47ef-bcca-2c0e5a46483d"),
//                25,
//                34,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//        val textView = item.findViewById<TextView>(R.id.url_text)
//        textView.text = spannableString
//        // enable clicking on url span
//        textView.movementMethod = LinkMovementMethod.getInstance()

        if (application != null) {
            AlertDialog.Builder(application, R.style.AlertDialogTheme)
                .setMessage(application.getString(R.string.welcome_to_use_text))
    //            .setView(item)
                .setPositiveButton(R.string.continue_text) { _, _ ->
                    leave()
    //                findNavController(fragment).navigate(MobileNavigationDirections.navigateToHomeFragment())
                    findNavController(fragment).navigate(MobileNavigationDirections.navigateToServiceFragment(hasData))
                }
                .show()
                .getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(application.getColor(R.color.button_balck))
        }
    }

}