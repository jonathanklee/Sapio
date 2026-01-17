package com.klee.sapio.domain.model

import android.graphics.drawable.Drawable
import java.util.Date

data class Evaluation(
    val name: String,
    val packageName: String,
    val iconUrl: String?,
    val rating: Int,
    val microg: Int,
    val secure: Int,
    val updatedAt: Date?,
    val createdAt: Date?,
    val publishedAt: Date?,
    val versionName: String?
)

data class UploadEvaluation(
    val name: String,
    val packageName: String,
    val icon: Int?,
    val rating: Int,
    val microg: Int,
    val rooted: Int
)

data class Icon(
    val id: Int,
    val name: String,
    val url: String
)

data class EvaluationRecord(
    val id: Int,
    val evaluation: Evaluation
)

data class InstalledApplication(
    val name: String,
    val packageName: String,
    val icon: Drawable
)
