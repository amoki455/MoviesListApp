package com.example.movielist.data.sources.remote

import com.example.movielist.data.models.Movie
import com.example.movielist.data.models.MovieCategory
import com.example.movielist.data.models.MovieDetails
import com.example.movielist.data.models.Keyword
import com.example.movielist.data.models.MovieImages
import com.example.movielist.network.MoviesApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface IRemoteSource {
    suspend fun getNowPlayingMovies(page: Int): List<Movie>

    suspend fun getPopularMovies(page: Int): List<Movie>

    suspend fun getTopRatedMovies(page: Int): List<Movie>

    suspend fun getUpcomingMovies(page: Int): List<Movie>

    suspend fun getMovieDetails(id: Int): MovieDetails

    suspend fun searchByKeywords(keywords: String, page: Int): List<Movie>

    suspend fun searchForKeywords(query: String): List<Keyword>

    suspend fun getImages(movieId: Int): MovieImages
}

class RemoteSource(private val apiService: MoviesApiService) : IRemoteSource {
    override suspend fun getNowPlayingMovies(page: Int): List<Movie> = withContext(Dispatchers.IO) {
        apiService.nowPlayingMovies(page).results?.map {
            it.copy(containedPage = page, category = MovieCategory.NOW_PLAYING)
        } ?: emptyList()
    }

    override suspend fun getPopularMovies(page: Int): List<Movie> = withContext(Dispatchers.IO) {
        apiService.popularMovies(page).results?.map {
            it.copy(containedPage = page, category = MovieCategory.POPULAR)
        } ?: emptyList()
    }

    override suspend fun getTopRatedMovies(page: Int): List<Movie> = withContext(Dispatchers.IO) {
        apiService.topMovies(page).results?.map {
            it.copy(containedPage = page, category = MovieCategory.TOP_RATED)
        } ?: emptyList()
    }

    override suspend fun getUpcomingMovies(page: Int): List<Movie> = withContext(Dispatchers.IO) {
        apiService.upcomingMovies(page).results?.map {
            it.copy(containedPage = page, category = MovieCategory.UPCOMING)
        } ?: emptyList()
    }

    override suspend fun getMovieDetails(id: Int): MovieDetails = withContext(Dispatchers.IO) {
        apiService.movie(id)
    }

    override suspend fun searchByKeywords(keywords: String, page: Int): List<Movie> =
        withContext(Dispatchers.IO) {
            apiService.searchByKeyWords(keywords, page).results ?: emptyList()
        }

    override suspend fun searchForKeywords(query: String): List<Keyword> =
        withContext(Dispatchers.IO) {
            apiService.queryKeywords(query).results ?: emptyList()
        }

    override suspend fun getImages(movieId: Int): MovieImages = withContext(Dispatchers.IO) {
        apiService.images(movieId)
    }
}