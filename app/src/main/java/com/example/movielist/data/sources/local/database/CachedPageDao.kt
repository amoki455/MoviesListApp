package com.example.movielist.data.sources.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movielist.data.models.MovieCategory
import com.example.movielist.data.models.db.CachedPage

@Dao
interface CachedPageDao {
    @Query("select * from cached_page where page = :page and category = :category")
    suspend fun findPage(page: Int, category: MovieCategory): CachedPage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(cachedPage: CachedPage)
}