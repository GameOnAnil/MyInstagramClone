package com.gameonanil.instagramcloneapp.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.gameonanil.imatagramcloneapp.databinding.MainRecyclerListBinding
import com.gameonanil.instagramcloneapp.models.Posts
import com.gameonanil.instagramcloneapp.models.User
import com.google.firebase.firestore.FirebaseFirestore


class MainRecyclerAdapter(val context: Context, private val postList: List<Posts>) :
    RecyclerView.Adapter<MainRecyclerAdapter.MainRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainRecyclerViewHolder {
        val binding =
            MainRecyclerListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainRecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainRecyclerViewHolder, position: Int) {
            holder.bindTo(postList[position])


    }


    override fun getItemCount() = postList.size


    inner class MainRecyclerViewHolder(private val binding: MainRecyclerListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindTo(posts: Posts) {
            binding.apply {
                description.text = posts.description


                binding.progressLoadingPic.isVisible = true
                Glide.with(context)
                    .load(posts.image_url)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.progressLoadingPic.isVisible = false
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.progressLoadingPic.isVisible = false
                            return false
                        }

                    })
                    .into(postImageHome)




                if (posts.posted_by != "") {
                    val documentRef = FirebaseFirestore.getInstance().collection("users")
                        .document(posts.posted_by)
                    documentRef.get().addOnSuccessListener {
                        val userData = it.toObject(User::class.java)
                        binding.userNamePoster.text = userData?.username
                        val publishedByText = "Published By: ${userData?.username}"
                        binding.tvPublisher.text = publishedByText
                        if (userData?.profile_image != "") {
                            Glide.with(context)
                                .load(userData!!.profile_image)
                                .into(civUserProfileMain)

                        }

                    }
                }

                tvPostedTime.text = DateUtils.getRelativeTimeSpanString(posts.creation_time_ms)


            }
        }

    }
}