package com.example.movielist

import android.app.Application
import androidx.room.Room
import com.example.movielist.data.repositories.MoviesRepository
import com.example.movielist.data.sources.local.database.AppDatabase
import com.example.movielist.data.sources.local.LocalSource
import com.example.movielist.data.sources.remote.RemoteSource
import com.example.movielist.network.ApiAdapter
import com.example.movielist.network.MoviesApiService

class ThisApp : Application() {
    companion object {
        lateinit var instance: ThisApp
            private set

        lateinit var moviesRepository: MoviesRepository
            private set

        /**
         * possible width values is  185 or 300 or 400 or 500
         */
        fun createImageUrl(filepath: String, width: Int = 300) = "https://image.tmdb.org/t/p/w$width/${filepath}"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).build()
        val apiService = ApiAdapter.instance.create(MoviesApiService::class.java)

        val localSource = LocalSource(database)
        val remoteSource = RemoteSource(apiService)
        moviesRepository = MoviesRepository(remoteSource, localSource)
    }
}