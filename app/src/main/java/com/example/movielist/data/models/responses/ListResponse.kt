package com.example.movielist.data.models.responses

import com.example.movielist.data.models.Movie

data class MovieListResponse(
    val page: Int = 0,
    val results: List<Movie> = emptyList(),
    val totalPages: Int = 0,
    val totalResults: Int = 0,
)