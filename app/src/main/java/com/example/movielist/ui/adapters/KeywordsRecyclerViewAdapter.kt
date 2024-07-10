package com.example.movielist.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.movielist.data.models.Keyword
import com.example.movielist.databinding.KeywordItemBinding

class KeywordsRecyclerViewAdapter :
    ListAdapter<Keyword, KeywordsRecyclerViewAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<Keyword>() {
            override fun areItemsTheSame(oldItem: Keyword, newItem: Keyword): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Keyword, newItem: Keyword): Boolean =
                oldItem == newItem
        }) {

    var onItemClick: (Keyword) -> Unit = {}

    inner class ViewHolder(val binding: KeywordItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            KeywordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val keyword = getItem(position)
        holder.binding.textview.text = keyword.name
        holder.binding.root.setOnClickListener { onItemClick(keyword) }
    }
}