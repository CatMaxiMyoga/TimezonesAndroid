package com.miyuki.timezones.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM people ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Person>>

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getById(id: Long): Person?

    @Query("SELECT * FROM people WHERE timezoneId = :timezoneId")
    suspend fun getForTimezone(timezoneId: Long): List<Person>

    @Insert
    suspend fun insert(person: Person): Long

    @Update
    suspend fun update(person: Person)

    @Delete
    suspend fun delete(person: Person)
}