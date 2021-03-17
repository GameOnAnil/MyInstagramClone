package com.gameonanil.instagramcloneapp.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.imatagramcloneapp.R
import com.gameonanil.instagramcloneapp.adapter.SearchRecyclerAdapter
import com.gameonanil.instagramcloneapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment(R.layout.fragment_search) {
    companion object{
        private const val TAG = "SearchFragment"
    }

    private lateinit var adapter: SearchRecyclerAdapter
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var userList: MutableList<User>
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**Setting Up Toolbar*/
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_search)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment,
                R.id.profileFragment,
                R.id.addPostFragment,
                R.id.searchFragment
            )
        )
        val navHostFragment = NavHostFragment.findNavController(this);
        NavigationUI.setupWithNavController(toolbar, navHostFragment,appBarConfiguration)

        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }


        userList = mutableListOf()
        firebaseFirestore = FirebaseFirestore.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser!!

        adapter = SearchRecyclerAdapter(requireActivity().baseContext,userList)
        recycler_search.adapter = adapter

        val collectionReference = firebaseFirestore.collection("users")

        collectionReference.addSnapshotListener{snapshot,exception->
            if (exception !=null || snapshot == null){
                Log.e(TAG, "onCreate: Exception: $exception", )
                return@addSnapshotListener
            }

            val userFromDb = snapshot.toObjects(User::class.java)
            userList.clear()
            for(user in userFromDb){
                if (user.uid!=currentUser!!.uid){
                    userList.add(user)
                    adapter.notifyDataSetChanged()
                }
            }


        }


    }

}
