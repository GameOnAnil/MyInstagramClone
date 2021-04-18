package com.gameonanil.instagramcloneapp.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.gameonanil.imatagramcloneapp.R
import com.gameonanil.instagramcloneapp.models.User
import com.gameonanil.instagramcloneapp.ui.start.StartActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var currentUser: FirebaseUser


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = toolbar_profile
        val navHostFragment = NavHostFragment.findNavController(this)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.profileFragment,
                R.id.addPostFragment,
            )
        )
        NavigationUI.setupWithNavController(toolbar, navHostFragment, appBarConfiguration)

        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        currentUser = FirebaseAuth.getInstance().currentUser!!


        buttonEditProfile.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment()
            findNavController().navigate(action)
        }


        loadInitialData()

    }

    @SuppressLint("SetTextI18n")
    private fun loadInitialData() {
        val currentUid = currentUser.uid
        val collectionReference = FirebaseFirestore.getInstance().collection("users")
        collectionReference.document(currentUid).get().addOnSuccessListener {
            val currentUserData = it.toObject(User::class.java)
            currentUserData?.let { user ->
                if (user.profile_image != "") {
                    Glide.with(requireContext())
                        .load(user.profile_image)
                        .into(civProfileFragment)
                }
                if (user.fullname != "") {
                    tvFullNameInProfile.text = "Full Name: ${user.fullname}"
                }
                if (user.bio != "") {
                    tvBio.text = "Bio: ${user.fullname}"
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_logout) {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, StartActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        return super.onOptionsItemSelected(item)
    }

}

