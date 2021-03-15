package com.gameonanil.instagramcloneapp.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gameonanil.imatagramcloneapp.R
import com.gameonanil.instagramcloneapp.ui.start.StartActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object{
        private const val TAG = "MainActivity"
    }


    private lateinit var navController: NavController



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container_main) as NavHostFragment
        navController = navHostFragment.navController
     /*   appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment,R.id.profileFragment,R.id.addPostFragment,R.id.editProfileFragment)
        )

        setSupportActionBar(toolbar_main)*/

//        setupActionBarWithNavController(navController,appBarConfiguration)
        bottom_nav.setupWithNavController(navController)


    }

    override fun onStart() {
        super.onStart()
       val currentUser =  FirebaseAuth.getInstance().currentUser
        if (currentUser !=null){
            Toast.makeText(this, "currentUser is uid: ${currentUser.uid} ", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        return  navController.navigateUp()|| super.onSupportNavigateUp()
    }
}