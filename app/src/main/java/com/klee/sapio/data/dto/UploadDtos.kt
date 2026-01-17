package com.klee.sapio.data.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UploadEvaluation(
    @JsonProperty("name") val name: String,
    @JsonProperty("packageName") val packageName: String,
    @JsonProperty("icon") var icon: Int?,
    @JsonProperty("rating") val rating: Int,
    @JsonProperty("microg") val microg: Int,
    @JsonProperty("rooted") val rooted: Int
)

data class UploadAnswer(
    @JsonProperty("data") val data: StrapiElement,
    @JsonProperty("meta") val meta: StrapiMeta?
)

data class UploadEvaluationHeader(
    @JsonProperty("data") var data: UploadEvaluation
)
