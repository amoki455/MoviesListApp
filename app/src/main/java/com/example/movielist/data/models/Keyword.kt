package com.example.movielist.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Keyword(
    val id: Int,
    val value: String? = ""
)