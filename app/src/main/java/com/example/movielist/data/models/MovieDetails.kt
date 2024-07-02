package com.example.movielist.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "movie_details")
data class MovieDetails(
    @PrimaryKey val id: Int = 0,
    @SerializedName("backdrop_path") val backdropPath: String? = "",
    @SerializedName("genres") val genres: List<Genre>? = emptyList(),
    val budget: Int,
    val revenue: Int,
    val homepage: String?,
    val title: String? = "",
    @SerializedName("original_title") val originalTitle: String? = "",
    @SerializedName("original_language") val originalLanguage: String? = "",
    val overview: String? = "",
    val popularity: Float = 0f,
    @SerializedName("poster_path") val posterPath: String? = "",
    @SerializedName("release_date") val releaseData: String?, // should be parsed as Date object but since we just display it so no problem
    @SerializedName("video") val isVideo: Boolean = false,
    @SerializedName("adult") val isAdult: Boolean = false,
    @SerializedName("vote_average") val voteAverage: Float = 0f,
    @SerializedName("vote_count") val voteCount: Int = 0,
    val status: String? = "",
    val tagline: String? = "",
    @SerializedName("spoken_languages") val spokenLanguages: List<SpokenLanguage>? = emptyList(),
    @SerializedName("production_companies") val productionCompanies: List<ProductionCompany>? = emptyList(),
    @SerializedName("production_countries") val productionCountries: List<ProductionCountry>? = emptyList(),
)