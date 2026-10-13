package com.miyuki.timezones.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "timezones")
data class Timezone(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ianaId: String,
    val displayName: String,
    val order: Int
)
