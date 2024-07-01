package com.example.movielist.data.sources.remote

import com.example.movielist.data.models.Movie
import com.example.movielist.data.models.MovieCategory
import com.example.movielist.data.models.MovieDetails
import com.example.movielist.network.MoviesApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface IRemoteSource {
    suspend fun getNowPlayingMovies(page: Int): List<Movie>

    suspend fun getPopularMovies(page: Int): List<Movie>

    suspend fun getTopRatedMovies(page: Int): List<Movie>

    suspend fun getUpcomingMovies(page: Int): List<Movie>

    suspend fun getMovieDetails(id: Int): MovieDetails
}

class RemoteSource(private val apiService: MoviesApiService) : IRemoteSource {
    override suspend fun getNowPlayingMovies(page: Int): List<Movie> = withContext(Dispatchers.IO) {
        apiService.nowPlayingMovies(page).results.map {
            it.copy(containedPage = page, category = MovieCategory.NOW_PLAYING)
        }
    }

    override suspend fun getPopularMovies(page: Int): List<Movie> = withContext(Dispatchers.IO) {
        apiService.popularMovies(page).results.map {
            it.copy(containedPage = page, category = MovieCategory.POPULAR)
        }
    }

    override suspend fun getTopRatedMovies(page: Int): List<Movie> = withContext(Dispatchers.IO) {
        apiService.topMovies(page).results.map {
            it.copy(containedPage = page, category = MovieCategory.TOP_RATED)
        }
    }

    override suspend fun getUpcomingMovies(page: Int): List<Movie> = withContext(Dispatchers.IO) {
        apiService.upcomingMovies(page).results.map {
            it.copy(containedPage = page, category = MovieCategory.UPCOMING)
        }
    }

    override suspend fun getMovieDetails(id: Int): MovieDetails = withContext(Dispatchers.IO) {
        apiService.movie(id)
    }
}