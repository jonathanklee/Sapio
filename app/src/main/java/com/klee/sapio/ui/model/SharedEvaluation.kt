package com.klee.sapio.ui.model

import android.graphics.Bitmap

data class SharedEvaluation(
    val name: String,
    val packageName: String,
    val icon: Bitmap,
    val ratingMicrog: Int,
    val ratingBareAOSP: Int,
    val brokenFeaturesMicrog: List<String>? = null,
    val brokenFeaturesBareAOSP: List<String>? = null
)
