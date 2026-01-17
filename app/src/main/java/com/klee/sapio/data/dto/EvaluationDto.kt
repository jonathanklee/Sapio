package com.klee.sapio.data.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

data class Evaluation(
    @JsonProperty("name") val name: String,
    @JsonProperty("packageName") val packageName: String,
    @JsonProperty("iconUrl") var iconUrl: String?,
    @JsonProperty("rating") val rating: Int,
    @JsonProperty("microg") val microg: Int,
    @JsonProperty("rooted") val secure: Int,
    @JsonProperty("updatedAt") val updatedAt: Date?,
    @JsonProperty("createdAt") val createdAt: Date?,
    @JsonProperty("publishedAt") val publishedAt: Date?,
    @JsonProperty("versionName") val versionName: String?
)
