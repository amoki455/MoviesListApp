package com.example.movielist.data.models

import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Entity(tableName = "movie", primaryKeys = ["id", "containedPage", "category"])
data class Movie(
    val id: Int = 0,
    val containedPage: Int = 0,
    val category: MovieCategory = MovieCategory.NOW_PLAYING,
    @SerializedName("backdrop_path") val backdropPath: String = "",
    @SerializedName("genre_ids") val genreIds: List<Int> = emptyList(),
    val title: String = "",
    @SerializedName("original_title") val originalTitle: String = "",
    @SerializedName("original_language") val originalLanguage: String = "",
    val overview: String = "",
    val popularity: Float = 0f,
    @SerializedName("poster_path") val posterPath: String = "",
    @SerializedName("release_date") val releaseData: String, // should be parsed as Date object but since we just display it so no problem
    @SerializedName("video") val isVideo: Boolean = false,
    @SerializedName("adult") val isAdult: Boolean = false,
    @SerializedName("vote_average") val voteAverage: Float = 0f,
    @SerializedName("vote_count") val voteCount: Int = 0,
)

enum class MovieCategory {
    NOW_PLAYING, POPULAR, TOP_RATED, UPCOMING
}