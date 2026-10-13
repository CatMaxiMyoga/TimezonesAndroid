package com.miyuki.timezones.data

import android.content.Context
import androidx.room3.Room

object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "timezones.db"
            ).build().also {
                instance = it
            }
        }
    }
}