package com.example.movielist.network

import com.example.movielist.data.models.MovieDetails
import com.example.movielist.data.models.responses.KeywordsListResponse
import com.example.movielist.data.models.responses.MovieListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// To get the image => https://image.tmdb.org/t/p/w{width value(can be 185 or 300 or 400 or 500)}/{image path from the api response}

interface MoviesApiService {
    @GET("movie/now_playing")
    suspend fun nowPlayingMovies(@Query("page") page: Int = 1): MovieListResponse

    @GET("movie/popular")
    suspend fun popularMovies(@Query("page") page: Int = 1): MovieListResponse

    @GET("movie/top_rated")
    suspend fun topMovies(@Query("page") page: Int = 1): MovieListResponse

    @GET("movie/upcoming")
    suspend fun upcomingMovies(@Query("page") page: Int = 1): MovieListResponse

    @GET("discover/movie")
    suspend fun searchByKeyWords(
        @Query("with_keywords") keywords: String,
        @Query("page") page: Int
    ): MovieListResponse

    @GET("movie/{id}")
    suspend fun movie(@Path("id") id: Int): MovieDetails

    @GET("search/keyword")
    suspend fun queryKeywords(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): KeywordsListResponse
}