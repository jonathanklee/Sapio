package com.klee.sapio.data.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class StrapiAnswer(
    @JsonProperty("data") val data: ArrayList<StrapiElement>,
    @JsonProperty("meta") val meta: StrapiMeta
)

data class StrapiMeta(
    @JsonProperty("pagination") val pagination: StrapiPagination?
)

data class StrapiPagination(
    @JsonProperty("page") val page: Int,
    @JsonProperty("pageSize") val pageSize: Int,
    @JsonProperty("pageCount") val pageCount: Int,
    @JsonProperty("total") val total: Int
)

data class StrapiImageElement(
    @JsonProperty("id") val id: Int,
    @JsonProperty("attributes") val attributes: RemoteImage
)

data class StrapiElement(
    @JsonProperty("id") val id: Int,
    @JsonProperty("attributes") val attributes: Evaluation
)

data class Icon(
    @JsonProperty("data") val data: StrapiImageElement?
)
