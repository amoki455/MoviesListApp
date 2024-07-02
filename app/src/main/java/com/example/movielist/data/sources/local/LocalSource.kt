package com.example.movielist.data.sources.local

import com.example.movielist.data.models.Movie
import com.example.movielist.data.models.MovieCategory
import com.example.movielist.data.models.MovieDetails
import com.example.movielist.data.models.db.CachedPage
import com.example.movielist.data.models.Keyword
import com.example.movielist.data.sources.local.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

interface ILocalSource {
    suspend fun getPage(page: Int, category: MovieCategory): List<Movie>

    suspend fun savePage(page: Int, category: MovieCategory, moviesList: List<Movie>)

    suspend fun getMovieDetails(id: Int): MovieDetails?

    suspend fun saveMovieDetails(details: MovieDetails)
}

class LocalSource(private val db: AppDatabase) : ILocalSource {

    companion object {
        const val CACHE_TIMEOUT_SECONDS = 4 * 60 * 60
    }

    override suspend fun getPage(page: Int, category: MovieCategory): List<Movie> =
        withContext(Dispatchers.IO) {
            db.cachedPageDao().findPage(page, category)?.let {
                if (Instant.now().epochSecond - it.timestamp >= CACHE_TIMEOUT_SECONDS) {
                    deletePage(page, category)
                    return@withContext emptyList()
                }
                return@withContext db.movieDao().getPage(page, category)
            }
            return@withContext emptyList()
        }

    override suspend fun savePage(page: Int, category: MovieCategory, moviesList: List<Movie>) =
        withContext(Dispatchers.IO) {
            db.cachedPageDao().insertOrUpdate(CachedPage(page, category, Instant.now().epochSecond))
            db.movieDao().insert(moviesList)
        }

    override suspend fun getMovieDetails(id: Int): MovieDetails? = withContext(Dispatchers.IO) {
        db.movieDetailsDao().getById(id)
    }

    override suspend fun saveMovieDetails(details: MovieDetails) = withContext(Dispatchers.IO) {
        db.movieDetailsDao().insertOrUpdate(details)
    }

    private suspend fun deletePage(page: Int, category: MovieCategory) =
        withContext(Dispatchers.IO) {
            val ids = mutableListOf<Int>()
            db.movieDao().getPage(page, category).forEach {
                ids.add(it.id)
            }
            db.movieDao().deletePage(page, category)
            db.movieDetailsDao().delete(ids)
        }
}