package com.miyuki.timezones.data

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [
        Person::class,
        Timezone::class,
        CustomField::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun timezoneDao(): TimezoneDao
    abstract fun customFieldDao(): CustomFieldDao
}