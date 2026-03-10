package com.klee.sapio.data.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

data class RemoteImage(
    @JsonProperty("name") val name: String,
    @JsonProperty("alternativeText") val alternativeText: String?,
    @JsonProperty("caption") val caption: String?,
    @JsonProperty("width") val width: Int,
    @JsonProperty("height") val height: Int,
    @JsonProperty("formats") val formats: RemoteImageFormats?,
    @JsonProperty("hash") val hash: String,
    @JsonProperty("ext") val ext: String,
    @JsonProperty("mime") val mime: String,
    @JsonProperty("size") val size: Int,
    @JsonProperty("url") val url: String,
    @JsonProperty("previewUrl") val previewUrl: String?,
    @JsonProperty("provider") val provider: String?,
    @JsonProperty("provider_metadata") val provider_metadata: String?,
    @JsonProperty("createdAt") val createdAt: Date,
    @JsonProperty("updatedAt") val updatedAt: Date
)

data class RemoteImageFormats(
    @JsonProperty("thumbnail") val thumbnail: Image,
    @JsonProperty("large") val large: Image?,
    @JsonProperty("medium") val medium: Image?,
    @JsonProperty("small") val small: Image?
)

data class Image(
    @JsonProperty("name") val name: String,
    @JsonProperty("hash") val hash: String,
    @JsonProperty("ext") val ext: String,
    @JsonProperty("mime") val mime: String,
    @JsonProperty("path") val path: String?,
    @JsonProperty("width") val width: Int,
    @JsonProperty("height") val height: Int,
    @JsonProperty("size") val size: Int,
    @JsonProperty("url") val url: String
)

data class IconAnswer(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("alternativeText") val alternativeText: String?,
    @JsonProperty("caption") val caption: String?,
    @JsonProperty("width") val width: Int,
    @JsonProperty("height") val height: Int,
    @JsonProperty("formats") val formats: RemoteImageFormats?,
    @JsonProperty("hash") val hash: String,
    @JsonProperty("ext") val ext: String,
    @JsonProperty("mime") val mime: String,
    @JsonProperty("size") val size: Int,
    @JsonProperty("url") val url: String,
    @JsonProperty("previewUrl") val previewUrl: String?,
    @JsonProperty("provider") val provider: String?,
    @JsonProperty("provider_metadata") val provider_metadata: String?,
    @JsonProperty("createdAt") val createdAt: Date,
    @JsonProperty("updatedAt") val updatedAt: Date
)
