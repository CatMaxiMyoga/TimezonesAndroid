package com.miyuki.timezones.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomFieldDao {

    @Query(
        """
        SELECT * FROM custom_fields
        WHERE personId = :personId
        ORDER BY id
        """,
    )
    fun observeForPerson(personId: Long): Flow<List<CustomField>>

    @Query("""
        SELECT DISTINCT name
        FROM custom_fields
        WHERE TRIM(name) != ''
        ORDER BY name COLLATE NOCASE
    """)
    fun observeAllNames(): Flow<List<String>>

    @Insert
    suspend fun insert(field: CustomField): Long

    @Update
    suspend fun update(field: CustomField)

    @Delete
    suspend fun delete(field: CustomField)

    @Query("DELETE FROM custom_fields WHERE personId = :personId")
    suspend fun deleteForPerson(personId: Long)
}