package com.example.movielist.data.sources.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movielist.data.models.MovieDetails

@Dao
interface MovieDetailsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: MovieDetails)

    @Query("select * from movie_details where id = :id")
    suspend fun getById(id: Int): MovieDetails?

    @Query("delete from movie_details where id in (:ids)")
    suspend fun delete(ids: List<Int>)
}