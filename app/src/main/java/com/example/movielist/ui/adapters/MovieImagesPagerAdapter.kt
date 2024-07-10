package com.example.movielist.ui.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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

    var onLoadedFirstImage: () -> Unit = {}

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
            var reqBuilder = Glide.with(it)
                .load(ThisApp.createImageUrl(img.filePath.toString(), 500))
                .placeholder(R.drawable.image_placeholder)

            if (position == 0) {
                reqBuilder = reqBuilder.listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        onLoadedFirstImage()
                        return false
                    }

                })
            }

            reqBuilder.into(it)
        }
    }
}