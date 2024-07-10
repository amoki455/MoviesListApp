package com.example.movielist.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movielist.R
import com.example.movielist.ThisApp
import com.example.movielist.data.models.MovieImage

class MovieImagesPagerAdapter :
    ListAdapter<MovieImage, MovieImagesPagerAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<MovieImage>() {
            override fun areItemsTheSame(oldItem: MovieImage, newItem: MovieImage): Boolean =
                oldItem.filePath == newItem.filePath

            override fun areContentsTheSame(oldItem: MovieImage, newItem: MovieImage): Boolean =
                oldItem == newItem
        }
    ) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView? = view.findViewById(R.id.image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.pager_image, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val img = getItem(position)
        holder.imageView?.let {
            Glide.with(it)
                .load(ThisApp.createImageUrl(img.filePath.toString(), 500))
                .placeholder(R.drawable.image_placeholder)
                .into(it)
        }
    }
}