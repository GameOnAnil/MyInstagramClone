package com.gameonanil.instagramcloneapp.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.gameonanil.imatagramcloneapp.R
import com.gameonanil.instagramcloneapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_edit_profile.*

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {
    companion object {
        private const val TAG = "EditProfileFragment"
        private const val GALLERY_REQUEST_CODE = 1234
    }

    private lateinit var collectionReference: CollectionReference
    private var currentUid: String? = null
    private var photoUri:Uri? = null
    private lateinit var storageReference: StorageReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**Setting Up toolbar*/
        val toolbar = toolbar_edit_profile
        val navHostFragment = NavHostFragment.findNavController(this)
        NavigationUI.setupWithNavController(toolbar,navHostFragment)


        collectionReference = FirebaseFirestore.getInstance().collection("users")
        currentUid = FirebaseAuth.getInstance().currentUser?.uid
        storageReference = FirebaseStorage.getInstance().reference


        /** loading initial data**/
        loadInfoOnStart()

        btnConfirmChanges.setOnClickListener {
            val fullNameString = etFullNameEdit.text.toString()
            val userNameString = etUserNameEditProfile.text.toString()
            val bioString = etBioEdit.text.toString()


            if (fullNameString.isBlank() || userNameString.isBlank()) {
                Toast.makeText(
                    activity,
                    "Please enter both username and full name",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (bioString.isBlank()) {
                Toast.makeText(activity, "Please say something in bio.", Toast.LENGTH_SHORT).show()
            } else {
                btnConfirmChanges.isEnabled =false
                progressbar_edit_profile.isVisible = true

                Log.d(TAG, "onViewCreated: confirm clicked!!!!!!!!!!!")
                handleConfirm(fullNameString,userNameString,bioString)
            }


        }

        btnChangeImage.setOnClickListener {
            val imagePickerIntent =
                Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerIntent.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
            imagePickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            imagePickerIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivityForResult(imagePickerIntent, GALLERY_REQUEST_CODE)

        }
    }

    private fun handleConfirm(fullNameString: String, userNameString: String, bioString: String) {
        if (currentUid == null) {
            Toast.makeText(activity, "user cannot be found", Toast.LENGTH_SHORT).show()
        } else {
            if (photoUri== null){
                 changeUserInfo(fullNameString, userNameString, bioString)
            }else{
                changeUriIntoWithPhoto(fullNameString,userNameString,bioString)
            }

        }

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        photoUri = uri
                        launchImageCrop(uri)

                    }
                } else {
                    Log.e(TAG, "onActivityResult: Image Selection Error")
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    result.uri?.let {
                        setImageView(it)
                    }

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.e(TAG, "onActivityResult: Crop Error: ${result.error}")
                }
            }
        }
    }

    private fun setImageView(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .into(civEditProfile)
    }

    private fun launchImageCrop(uri: Uri) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .start(requireContext(), this)
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
            if (currentUser.profile_image!=""){
               setImageView(currentUser.profile_image.toUri())
            }
        }
    }

    private fun changeUserInfo(fullNameString: String, userNameString: String, bioString: String) {
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
                progressbar_edit_profile.isVisible = false
                Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }.addOnFailureListener{
                progressbar_edit_profile.isVisible = false
                Toast.makeText(context, "Profile Updated Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener {
            btnConfirmChanges.isEnabled = true
            progressbar_edit_profile.isVisible = false
            Toast.makeText(context, "Profile Updated Failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }

    }

    private fun changeUriIntoWithPhoto(
        fullNameString: String,
        userNameString: String,
        bioString: String
    ) {
        val photoReference = storageReference.child("/profile_images/p_image${System.currentTimeMillis()}-photo.jpg")
        photoReference.putFile(photoUri!!).continueWithTask { photoUploadTask->
            photoReference.downloadUrl

        }.continueWithTask { downloadUrlTask->
            val documentReference = collectionReference.document(currentUid!!)
            documentReference.get().addOnSuccessListener {
                val currentUser = it.toObject(User::class.java)
                var newUser = User(
                    email = currentUser!!.email,
                    profile_image = downloadUrlTask.result.toString(),
                    uid = currentUser.uid,
                    username = userNameString,
                    fullname = fullNameString,
                    bio = bioString,
                )
                documentReference.set(newUser).addOnSuccessListener {
                    findNavController().navigateUp()
                }.addOnFailureListener{
                    progressbar_edit_profile.isVisible = false
                    Toast.makeText(context, "Profile Updated Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            btnConfirmChanges.isEnabled = true
            progressbar_edit_profile.isVisible = false
            Toast.makeText(context, "Profile Updated Failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

}