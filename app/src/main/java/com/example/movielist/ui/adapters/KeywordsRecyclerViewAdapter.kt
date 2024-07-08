package com.example.movielist.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movielist.data.models.Keyword
import com.example.movielist.databinding.KeywordItemBinding

class KeywordsRecyclerViewAdapter(var keywords: MutableList<Keyword>) :
    RecyclerView.Adapter<KeywordsRecyclerViewAdapter.ViewHolder>() {

    var onItemClick: (Keyword) -> Unit = {}

    inner class ViewHolder(val binding: KeywordItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            KeywordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return keywords.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val keyword = keywords[position]
        holder.binding.textview.text = keyword.name
        holder.binding.root.setOnClickListener { onItemClick(keyword) }
    }
}