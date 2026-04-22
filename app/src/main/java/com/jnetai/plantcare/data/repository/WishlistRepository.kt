package com.jnetai.plantcare.data.repository

import com.jnetai.plantcare.data.dao.WishlistDao
import com.jnetai.plantcare.data.entity.WishlistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WishlistRepository(private val wishlistDao: WishlistDao) {

    fun getAllItems(): Flow<List<WishlistItem>> = wishlistDao.getAllItems()

    suspend fun insert(item: WishlistItem): Long = withContext(Dispatchers.IO) {
        wishlistDao.insert(item)
    }

    suspend fun delete(item: WishlistItem) = withContext(Dispatchers.IO) {
        wishlistDao.delete(item)
    }

    suspend fun getAllItemsList(): List<WishlistItem> = withContext(Dispatchers.IO) {
        wishlistDao.getAllItemsList()
    }
}