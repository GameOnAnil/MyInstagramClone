package com.gameonanil.instagramcloneapp.adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gameonanil.imatagramcloneapp.databinding.MainRecyclerListBinding

import com.gameonanil.instagramcloneapp.models.Posts
import com.gameonanil.instagramcloneapp.models.User
import com.google.firebase.firestore.FirebaseFirestore


class MainRecyclerAdapter( val context: Context, private val postList: List<Posts>):
    RecyclerView.Adapter<MainRecyclerAdapter.MainRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainRecyclerViewHolder {
      val binding = MainRecyclerListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MainRecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainRecyclerViewHolder, position: Int) {
       holder.bindTo(postList[position])
    }



    override fun getItemCount() = postList.size


    inner class MainRecyclerViewHolder(private val binding: MainRecyclerListBinding): RecyclerView.ViewHolder(binding.root){
        fun bindTo(posts: Posts) {
            binding.apply {
                description.text = posts.description

                Glide.with(context)
                    .load(posts.image_url)
                    .into(postImageHome)

                if (posts.posted_by !=""){
                    val documentRef = FirebaseFirestore.getInstance().collection("users").document(posts.posted_by)
                    documentRef.get().addOnSuccessListener {
                        val userData = it.toObject(User::class.java)
                        binding.userNamePoster.text = userData?.username
                        if (userData?.profile_image !=""){
                           Glide.with(context)
                               .load(userData!!.profile_image)
                               .into(civUserProfileMain)
                        }

                    }
                }


            }
        }

    }
}