package com.example.movielist.data.models.responses

import com.example.movielist.data.models.Movie
import com.example.movielist.data.models.Keyword

data class MovieListResponse(
    val page: Int = 0,
    val results: List<Movie>? = emptyList(),
    val totalPages: Int = 0,
    val totalResults: Int = 0,
)

data class KeywordsListResponse(
    val page: Int = 0,
    val results: List<Keyword>? = emptyList(),
    val totalPages: Int = 0,
    val totalResults: Int = 0,
)