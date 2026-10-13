package com.miyuki.timezones.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimezoneDao {
    @Query("SELECT * FROM timezones ORDER BY `order`")
    fun observeAll(): Flow<List<Timezone>>

    @Query("SELECT * FROM timezones ORDER BY `order`")
    suspend fun getAll(): List<Timezone>

    @Query("SELECT * FROM timezones WHERE id = :id")
    suspend fun getById(id: Long): Timezone?

    @Query("SELECT COALESCE(MAX(`order`) + 1, 0) FROM timezones")
    suspend fun getNextOrder(): Int

    @Insert
    suspend fun insert(timezone: Timezone): Long

    @Update
    suspend fun update(timezone: Timezone)

    @Delete
    suspend fun delete(timezone: Timezone)
}