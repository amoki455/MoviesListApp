package com.example.movielist.data.models.db

import androidx.room.Entity
import com.example.movielist.data.models.MovieCategory

@Entity(tableName = "cached_page", primaryKeys = ["page", "category"])
data class CachedPage(
    val page: Int,
    val category: MovieCategory,
    val timestamp: Long
)
