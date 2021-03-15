package com.gameonanil.instagramcloneapp.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.bumptech.glide.util.Util
import com.gameonanil.imatagramcloneapp.R
import com.gameonanil.instagramcloneapp.models.Posts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_add_post.*
import kotlinx.android.synthetic.main.main_recycler_list.*

class AddPostFragment : Fragment(R.layout.fragment_add_post) {
    companion object {
        private const val PICKER_REQUEST_CODE = 111
        private const val TAG = "AddPostFragment"
    }

    private var photoUri: Uri? = null
    private lateinit var storageReference: StorageReference
    private lateinit  var currentUser : FirebaseUser
    private lateinit var appBarConfiguration: AppBarConfiguration
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setting Up Toolbar
        val toolbar = toolbar_add_post
        val navHostFragment = NavHostFragment.findNavController(this)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment,
                R.id.profileFragment,
                R.id.addPostFragment,
            )
        )
        NavigationUI.setupWithNavController(toolbar,navHostFragment,appBarConfiguration)


        storageReference = FirebaseStorage.getInstance().reference
        currentUser = FirebaseAuth.getInstance().currentUser!!

        btnSubmit.setOnClickListener {
            handleSubmitButton()
        }

        btnChoosePhoto.setOnClickListener {
            val intentImagePicker = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intentImagePicker.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
            intentImagePicker.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            intentImagePicker.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivityForResult(intentImagePicker, PICKER_REQUEST_CODE)
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            //for image picker
            PICKER_REQUEST_CODE->{
                if (resultCode == Activity.RESULT_OK) {
                   data?.data?.let {
                       photoUri = it                    
                      launchImageCropper(it)
                   }
                }
            }
            //for image cropper
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    result.uri?.let {
                        setImage(it)
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.e(TAG, "onActivityResult: Crop Error: ${result.error}",)
                }
            }

        }

    }

    private fun setImage(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .into(ivPostImage)

    }

    private fun launchImageCropper(uri: Uri) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(requireContext(),this)
    }

    private fun handleSubmitButton() {
        val description = etEnterDescription.text.toString()
        if (photoUri == null) {
            Toast.makeText(context, "Please chose a picture", Toast.LENGTH_SHORT).show()
            return
        } else if (description.isBlank()) {
            Toast.makeText(context, "Please Enter Description", Toast.LENGTH_SHORT).show()
            return
        } else {

            val photoReference =
                storageReference.child("/post_images/${System.currentTimeMillis()}-photo.jpg")
            photoReference.putFile(photoUri!!)
                .continueWithTask { photoUploadTask ->

                    //Retrieve image url of the uploaded image
                    photoReference.downloadUrl
                }.continueWithTask { downloadUrlTask ->
                    //Create a post object with the image url and add it to the collection
                    val post = Posts(
                        System.currentTimeMillis(),
                        description,
                        downloadUrlTask.result.toString(),
                        currentUser.uid
                    )
                    FirebaseFirestore.getInstance().collection("posts").add(post)

                }.addOnCompleteListener { postCreationTask ->
                    if (!postCreationTask.isSuccessful) {
                        Log.e(TAG, "handleSubmitButton: exception: ", postCreationTask.exception)
                        Toast.makeText(activity, "faled to save post", Toast.LENGTH_SHORT).show()
                    }
                    etEnterDescription.text!!.clear()
                    ivPostImage.setImageResource(0)
                    Toast.makeText(activity, "Success!!", Toast.LENGTH_SHORT).show()

                }


        }
    }
}
