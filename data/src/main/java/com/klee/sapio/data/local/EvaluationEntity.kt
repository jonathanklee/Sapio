package com.klee.sapio.data.local

import androidx.room.Entity
import java.util.Date

@Entity(primaryKeys = ["packageName", "microg", "secure"])
data class EvaluationEntity(
    val name: String,
    val packageName: String,
    val iconUrl: String?,
    val rating: Int,
    val microg: Int,
    val secure: Int,
    val updatedAt: Date?,
    val createdAt: Date?,
    val publishedAt: Date?,
    val versionName: String?,
    val cachedAt: Long
)

@Entity(primaryKeys = ["id"])
data class IconEntity(
    val id: Int,
    val name: String,
    val url: String,
    val cachedAt: Long
)
