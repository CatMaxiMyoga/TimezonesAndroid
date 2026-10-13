package com.miyuki.timezones.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "custom_fields")
data class CustomField(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personId: Long,
    val name: String,
    val value: String
)
