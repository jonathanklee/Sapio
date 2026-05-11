package com.klee.sapio.domain.model

data class CachedDeviceApp(
    val packageName: String,
    val rating: Int?,
    val cachedAt: Long
)
