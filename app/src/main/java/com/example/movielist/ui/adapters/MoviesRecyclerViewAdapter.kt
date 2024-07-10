package com.example.movielist.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.example.movielist.R
import com.example.movielist.ThisApp
import com.example.movielist.data.models.Movie
import com.example.movielist.databinding.MovieBinding


class MoviesRecyclerViewAdapter : ListAdapter<Movie, MoviesRecyclerViewAdapter.ViewHolder>(object :
    DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean = oldItem == newItem
}) {

    var onReachedLastItem: () -> Unit = {}
    var onItemClick: (item: Movie) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding: MovieBinding? = null
        val root = when (viewType) {
            1 -> {
                binding = MovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                binding.root
            }

            2 -> LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            else -> throw IndexOutOfBoundsException()
        }
        return ViewHolder(root, binding)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).id == 0) 2 else 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding?.apply {
            root.setOnClickListener { onItemClick(item) }
            title.text = item.title
            vote.text = item.voteAverage.toString()
            item.posterPath?.let {
                Glide.with(this.root)
                    .load(ThisApp.createImageUrl(it))
                    .placeholder(R.drawable.image_placeholder)
                    .into(imageView)
            }
        }

        if (position == itemCount - 1) {
            onReachedLastItem()
        }
    }

    inner class ViewHolder(view: View, val binding: MovieBinding?) :
        RecyclerView.ViewHolder(view)
}