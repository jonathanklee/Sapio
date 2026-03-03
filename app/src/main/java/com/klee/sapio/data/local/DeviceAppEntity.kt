package com.klee.sapio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DeviceAppEntity(
    @PrimaryKey val packageName: String,
    val rating: Int?,
    val cachedAt: Long
)
