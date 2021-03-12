package com.gameonanil.instagramcloneapp.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import com.gameonanil.imatagramcloneapp.R
import com.gameonanil.instagramcloneapp.adapter.MainRecyclerAdapter
import com.gameonanil.instagramcloneapp.models.Posts
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.*

open class HomeFragment : Fragment(R.layout.fragment_home) {
    companion object{
        private const val TAG = "HomeFragment"
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: MainRecyclerAdapter
    private lateinit var postList: MutableList<Posts>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        postList = mutableListOf()
        adapter = MainRecyclerAdapter(requireActivity().baseContext,postList)

        recycler_main.adapter = adapter

        val collectionReference = firestore
            .collection("posts")
            .orderBy("creation_time_ms")

      /*  collectionReference.get().addOnSuccessListener { snapShot->
            val postFromDB = snapShot.toObjects(Posts::class.java)
            postList.addAll(postFromDB)
            adapter.notifyDataSetChanged()

        }*/

        collectionReference.addSnapshotListener{snapshot, exception ->
            if (exception !=null || snapshot == null){
                Log.e(TAG, "onCreate: Exception: $exception", )
                return@addSnapshotListener
            }

            val postFromDb =  snapshot.toObjects(Posts::class.java)
            //updating out list
            postList.clear()
            postList.addAll(postFromDb)
            adapter.notifyDataSetChanged()

        }

    }

}