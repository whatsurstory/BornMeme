package com.beva.bornmeme.ui.detail.dialog

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import com.beva.bornmeme.R
import com.beva.bornmeme.databinding.DialogCommentBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber
import java.util.*

class PublishCommentDialog: AppCompatDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.PublishDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DialogCommentBinding.inflate(inflater, container, false)

        binding.buttonPublish.setOnClickListener {

            if (binding.editPublishContent.text.isNullOrEmpty()) {
                Snackbar.make(it, "Not Adding Text Yet", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()

            } else {
                val fireStore = FirebaseFirestore.getInstance().collection("Posts")
                val document = fireStore.document()

                val publish = hashMapOf(
                    "commentId" to document.id,
                    "content" to binding.editPublishContent.text.toString(),
                    "ownerId" to "Beva9487",
                    "dislike" to null,
                    "like" to null,
                    "parentId" to "",
                    "photoUrl" to "",
                    "postId" to "",
                    "time" to Date(Calendar.getInstance().timeInMillis),
                    "userId" to "Beva9487"
                )
                Timber.d("publish data list: $publish")
                document.set(publish)
                    .addOnSuccessListener {
                        Timber.d("Publish Done")
                        dismiss()
                    }
                    .addOnFailureListener {
                        Timber.d("Error $it")
                    }
            }
        }

        return binding.root
    }
}