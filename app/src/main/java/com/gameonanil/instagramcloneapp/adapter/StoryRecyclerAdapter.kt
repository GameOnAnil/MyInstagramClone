package com.gameonanil.instagramcloneapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gameonanil.imatagramcloneapp.databinding.MainStoryRecyclerListBinding
import com.gameonanil.instagramcloneapp.models.User

class StoryRecyclerAdapter(private val context: Context, val userList: List<User>):
    RecyclerView.Adapter<StoryRecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MainStoryRecyclerListBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(userList[position])
    }

    override fun getItemCount(): Int {
      return userList.size
    }

    inner class ViewHolder(val binding:MainStoryRecyclerListBinding): RecyclerView.ViewHolder(binding.root){
        fun bindTo(user: User) {
            binding.apply {
                if (user.profile_image!=""){
                    Glide.with(context)
                        .load(user.profile_image)
                        .into(civStory)
                }
            }
        }


    }

}