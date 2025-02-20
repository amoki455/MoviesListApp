package com.example.movielist.data.repositories

import com.example.movielist.data.models.Movie
import com.example.movielist.data.models.MovieCategory
import com.example.movielist.data.models.MovieDetails
import com.example.movielist.data.models.Keyword
import com.example.movielist.data.models.MovieImage
import com.example.movielist.data.models.MovieImages
import com.example.movielist.data.sources.local.ILocalSource
import com.example.movielist.data.sources.remote.IRemoteSource

class MoviesRepository(
    private val remoteSource: IRemoteSource,
    private val localSource: ILocalSource
) {
    suspend fun getMoviesList(page: Int, category: MovieCategory): List<Movie> {
        val localData = localSource.getPage(page, category)
        if (localData.isNotEmpty())
            return localData

        val remoteData = when (category) {
            MovieCategory.NOW_PLAYING -> remoteSource.getNowPlayingMovies(page)
            MovieCategory.POPULAR -> remoteSource.getPopularMovies(page)
            MovieCategory.TOP_RATED -> remoteSource.getTopRatedMovies(page)
            MovieCategory.UPCOMING -> remoteSource.getUpcomingMovies(page)
            MovieCategory.UNCATEGORIZED -> emptyList()
        }
        localSource.savePage(page, category, remoteData)
        return remoteData
    }

    suspend fun getMovieDetails(id: Int): MovieDetails {
        localSource.getMovieDetails(id)?.let { return it }
        remoteSource.getMovieDetails(id).let {
            localSource.saveMovieDetails(it)
            return it
        }
    }

    suspend fun getMovieImages(movieId: Int): List<MovieImage> {
        return remoteSource.getImages(movieId).backdrops ?: emptyList()
    }

    /**
     * Searching always uses remote source
     */
    suspend fun searchByKeywords(keywords: String, page: Int): List<Movie> {
        return remoteSource.searchByKeywords(keywords, page)
    }

    suspend fun findKeywords(text: String): List<Keyword> {
        return remoteSource.searchForKeywords(text)
    }
}