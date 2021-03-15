package com.gameonanil.instagramcloneapp.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.imatagramcloneapp.R
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private lateinit var appBarConfiguration: AppBarConfiguration


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = toolbar_profile
        val navHostFragment = NavHostFragment.findNavController(this)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment,
                R.id.profileFragment,
                R.id.addPostFragment,
            )
        )
        NavigationUI.setupWithNavController(toolbar,navHostFragment,appBarConfiguration)


        buttonEditProfile.setOnClickListener {
           val action = ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment()
            findNavController().navigate(action)
        }

    }

}