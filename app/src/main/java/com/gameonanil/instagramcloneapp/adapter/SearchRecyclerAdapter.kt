package com.gameonanil.instagramcloneapp.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gameonanil.imatagramcloneapp.databinding.SearchRecyclerListBinding
import com.gameonanil.instagramcloneapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class SearchRecyclerAdapter(private val context: Context, val userList: List<User>) :
    RecyclerView.Adapter<SearchRecyclerAdapter.SearchViewModel>() {
    companion object {
        private const val TAG = "SearchRecyclerAdapter"
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewModel {
        val binding = SearchRecyclerListBinding.inflate(LayoutInflater.from(context), parent, false)
        return SearchViewModel(binding)
    }

    override fun onBindViewHolder(holder: SearchViewModel, position: Int) {
        holder.bindTo(userList[position])
    }

    override fun getItemCount() = userList.size


    inner class SearchViewModel(private val binding: SearchRecyclerListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        private val collectionRef = FirebaseFirestore.getInstance().collection("Follows")

        fun bindTo(user: User) {
            binding.apply {
                if (user.profile_image != "") {
                    Glide.with(context)
                        .load(user.profile_image)
                        .into(civSearchFragment)
                }

                userNameSearchFrag.text = user.username
                checkInitFollowing(user)

                btnFollow.setOnClickListener {
                    if (btnFollow.text == "Follow") {
                        handleFollow(user)

                    } else {
                        handleUnfollow(user)
                    }
                }

            }
        }

        fun checkInitFollowing(user: User){
           collectionRef.document(currentUser!!.uid).collection("Following").document(user.uid).get().addOnSuccessListener {
               if (it.contains("following")){
                   binding.btnFollow.text = "UnFollow"
               }

           }

        }


        private fun handleFollow(user: User) {
            currentUser?.uid.let { uid1 ->
             val hashMap = HashMap<String, Any>()
             hashMap["following"] = true
                //create and set following collection
                collectionRef.document(uid1!!)
                    .collection("Following").document(user.uid)
                    .set(hashMap).addOnSuccessListener {
                        Log.e(TAG, "bindTo:Following set  successful ")
                        //setting up followers collection
                        collectionRef.document(user.uid).collection("Followers").document(uid1)
                            .set(hashMap).addOnSuccessListener {
                                Log.e(TAG, "bindTo: Setting Followers successful")
                                binding.btnFollow.text = "UnFollow"
                            }

                    }.addOnFailureListener {
                        Toast.makeText(context, "Failure due to ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }

        private fun handleUnfollow(user: User) {
            currentUser?.uid.let { uid1 ->
                val hashMap = HashMap<String, Any>()
                hashMap["following"] = true
                //create and set following collection
                collectionRef.document(uid1!!).collection("Following").document(user.uid).delete()
                    .addOnSuccessListener {
                        collectionRef.document(user.uid).collection("Followers").document(uid1)
                            .delete().addOnSuccessListener {
                            Log.d(TAG, "handleUnfollow: Delete successful!")
                            binding.btnFollow.text = "Follow"
                        }
                    }

            }
        }

    }

}