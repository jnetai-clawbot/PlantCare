package com.jnetai.plantcare.data.dao

import androidx.room.*
import com.jnetai.plantcare.data.entity.WishlistItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<WishlistItem>>

    @Insert
    suspend fun insert(item: WishlistItem): Long

    @Delete
    suspend fun delete(item: WishlistItem)

    @Query("SELECT * FROM wishlist")
    suspend fun getAllItemsList(): List<WishlistItem>
}