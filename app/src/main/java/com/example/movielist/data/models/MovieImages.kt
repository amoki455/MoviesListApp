package com.example.movielist.data.models

import com.google.gson.annotations.SerializedName

data class MovieImages(
    val backdrops: List<MovieImage>? = emptyList()
)

data class MovieImage(
    @SerializedName("aspect_ratio") val aspectRatio: Float = 0f,
    val height: Int = 0,
    val width: Int = 0,
    @SerializedName("file_path") val filePath: String? = "",
    @SerializedName("vote_average") val averageVote: Float = 0f,
    @SerializedName("vote_count") val voteCount: Int = 0,
)