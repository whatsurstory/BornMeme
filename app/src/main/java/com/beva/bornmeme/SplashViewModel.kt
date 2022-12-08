package com.beva.bornmeme

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
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

    fun queryUserByUid(user: FirebaseUser, context: Context, fragment: SplashFragment) {
        var hasData = false
        val ref = Firebase.firestore
            .collection(context.getString(R.string.user_collection_text))
            .document(user.uid)

        Firebase.firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
//            val loginTimes = n + 1
            if (snapshot.data != null) {
                val checkUser = snapshot.toObject(User::class.java)
                if (checkUser != null) {
                    UserManager.user = checkUser
                }
                hasData = true
//                transaction.update(ref,FieldValue.arrayUnion(loginTimes))
            } else {
                val userData = User(
                    userId = user.uid,
                    profilePhoto = user.photoUrl.toString(),
                    userName = user.displayName.toString(),
                    email = user.email.toString(),
                    registerTime = Timestamp.now()
                )
                transaction.set(ref, userData)
                UserManager.user = userData
                hasData = false
            }
        }.addOnSuccessListener {
            getRegisterInfo(hasData, fragment, context)

        }.addOnFailureListener {
            Timber.d("ERROR ${it.message}")
            Toast.makeText(context, it.message.toString(), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getRegisterInfo(hasData: Boolean, fragment: SplashFragment, context: Context) {
        if (hasData) {
            leave()
            findNavController(fragment)
                .navigate(MobileNavigationDirections.navigateToHomeFragment())
        } else {
            showAgreeDialog(context, fragment)
        }
    }

    private fun showAgreeDialog(context: Context, fragment: SplashFragment) {

        val item = LayoutInflater.from(context)
            .inflate(R.layout.service_text, null)

        // set up spanned string with url
        val spannableString =
            SpannableString(context.getString(R.string.service_text_with_url))

        spannableString
            .setSpan(
                URLSpan("https://www.privacypolicies.com/live/679fc734-40b3-47ef-bcca-2c0e5a46483d"),
                25,
                34,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        val textView = item.findViewById<TextView>(R.id.url_text)
        textView.text = spannableString
        // enable clicking on url span
        textView.movementMethod = LinkMovementMethod.getInstance()

        AlertDialog.Builder(context, R.style.AlertDialogTheme)
            .setView(item)
            .setPositiveButton(R.string.agreeAndContinue) { _, _ ->
                leave()
                findNavController(fragment).navigate(MobileNavigationDirections.navigateToHomeFragment())
            }
            .show()
            .getButton(DialogInterface.BUTTON_POSITIVE)
            .setTextColor(context.getColor(R.color.button_balck))
    }

}