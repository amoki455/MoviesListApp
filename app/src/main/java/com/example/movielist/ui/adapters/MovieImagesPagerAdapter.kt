package com.example.movielist.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movielist.R
import com.example.movielist.ThisApp
import com.example.movielist.data.models.MovieImage

class MovieImagesPagerAdapter(var images: MutableList<MovieImage>) :
    RecyclerView.Adapter<MovieImagesPagerAdapter.ViewHolder>() {


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView? = view.findViewById(R.id.image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.pager_image, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val img = images[position]
        holder.imageView?.let {
            Glide.with(it)
                .load(ThisApp.createImageUrl(img.filePath.toString(), 500))
                .placeholder(R.drawable.image_placeholder)
                .into(it)
        }
    }
}