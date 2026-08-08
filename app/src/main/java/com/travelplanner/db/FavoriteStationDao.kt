package com.travelplanner.db

import androidx.room.*
import com.travelplanner.model.FavoriteStation
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStationDao {
    @Query("SELECT * FROM favorite_stations ORDER BY name ASC")
    fun getAllFavoritesFlow(): Flow<List<FavoriteStation>>

    @Query("SELECT * FROM favorite_stations ORDER BY name ASC")
    suspend fun getAllFavorites(): List<FavoriteStation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteStation)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteStation)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE crs = :crs)")
    fun isFavoriteFlow(crs: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE crs = :crs)")
    suspend fun isFavorite(crs: String): Boolean
}
