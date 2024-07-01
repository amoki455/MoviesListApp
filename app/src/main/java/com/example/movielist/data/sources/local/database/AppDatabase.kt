package com.example.movielist.data.sources.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.movielist.data.models.Movie
import com.example.movielist.data.models.MovieDetails
import com.example.movielist.data.models.db.CachedPage

@Database(entities = [CachedPage::class, Movie::class, MovieDetails::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedPageDao(): CachedPageDao
    abstract fun movieDao(): MovieDao
    abstract fun movieDetailsDao(): MovieDetailsDao
}