package com.klee.sapio.data.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

@JsonIgnoreProperties(ignoreUnknown = true)
data class Evaluation(
    @JsonProperty("name") val name: String,
    @JsonProperty("packageName") val packageName: String,
    @JsonProperty("icon") val icon: Icon?,
    @JsonProperty("rating") val rating: Int,
    @JsonProperty("microg") val microg: Int,
    @JsonProperty("rooted") val secure: Int,
    @JsonProperty("updatedAt") val updatedAt: Date?,
    @JsonProperty("createdAt") val createdAt: Date?,
    @JsonProperty("publishedAt") val publishedAt: Date?,
    @JsonProperty("versionName") val versionName: String?,
    @JsonProperty("brokenFeatures") val brokenFeatures: List<String>?,
    @JsonProperty("os") val os: String? = null,
    @JsonProperty("osVersion") val osVersion: String? = null,
    @JsonProperty("androidVersion") val androidVersion: Int? = null
)
