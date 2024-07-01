package com.example.movielist.data.sources.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movielist.data.models.Movie
import com.example.movielist.data.models.MovieCategory

@Dao
interface MovieDao {
    @Query("select * from movie where containedPage = :page and category = :category")
    suspend fun getPage(page: Int, category: MovieCategory): List<Movie>

    @Query("delete from movie where containedPage = :page and category = :category")
    suspend fun deletePage(page: Int, category: MovieCategory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(movies: List<Movie>)
}