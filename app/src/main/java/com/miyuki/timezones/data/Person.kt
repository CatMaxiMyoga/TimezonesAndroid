package com.miyuki.timezones.data

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "people",
    foreignKeys = [
        ForeignKey(
            entity = Timezone::class,
            parentColumns = ["id"],
            childColumns = ["timezoneId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("timezoneId")
    ]
)
data class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val nickname: String?,
    val timezoneId: Long
)
