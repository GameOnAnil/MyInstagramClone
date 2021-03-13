package com.gameonanil.instagramcloneapp.ui.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gameonanil.imatagramcloneapp.R
import com.gameonanil.instagramcloneapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_signup.*

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {
    companion object {
        private const val TAG = "EditProfileFragment"
    }

    private lateinit var collectionReference: CollectionReference
    private var currentUid: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectionReference = FirebaseFirestore.getInstance().collection("users")
        currentUid = FirebaseAuth.getInstance().currentUser?.uid

        //todo loading initial data
        loadInfoOnStart()

        btnConfirmChanges.setOnClickListener {
            val fullNameString = etFullNameEdit.text.toString()
            val userNameString = etUserNameEditProfile.text.toString()
            val bioString = etBioEdit.text.toString()

            if (fullNameString.isBlank() || userNameString.isBlank()) {
                Toast.makeText(
                    context,
                    "Please enter both username and full name",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (bioString.isBlank()) {
                Toast.makeText(context, "Please say something in bio.", Toast.LENGTH_SHORT).show()
            } else {
                if (currentUid == null) {
                    Toast.makeText(context, "user cannot be found", Toast.LENGTH_SHORT).show()
                } else {
                    changeUserInfo(fullNameString, userNameString, bioString)
                }
            }


        }
    }

    private fun loadInfoOnStart() {
        collectionReference.document(currentUid!!).get().addOnSuccessListener {
            val currentUser = it.toObject(User::class.java)
            etUserNameEditProfile.setText(currentUser!!.username)
            if (currentUser.bio != "") {
                etBioEdit.setText(currentUser.bio)
            }
            if (currentUser.fullname != "") {
                etFullNameEdit.setText(currentUser.fullname)
            }
        }
    }


    private fun changeUserInfo(fullNameString: String,userNameString: String,bioString: String) {
        val documentReference = collectionReference.document(currentUid!!)

        documentReference.get().addOnSuccessListener {
            val currentUser = it.toObject(User::class.java)

            var newUser: User = User(
                email = currentUser!!.email,
                profile_image = currentUser.profile_image,
                uid = currentUser.uid,
                username = userNameString,
                fullname = fullNameString,
                bio = bioString,
            )

            documentReference.set(newUser).addOnSuccessListener {
                Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
            }

        }

    }

}