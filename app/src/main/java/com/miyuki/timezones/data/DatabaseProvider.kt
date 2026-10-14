package com.miyuki.timezones.data

import android.content.Context
import androidx.room3.Room
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

object DatabaseProvider {
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE people ADD COLUMN description TEXT")
        }
    }

    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "timezones.db"
            ).addMigrations(MIGRATION_2_3).build().also {
                instance = it
            }
        }
    }
}