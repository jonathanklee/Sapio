package com.klee.sapio.ui.view

data class EvaluationHistoryRow(
    val rating: Int,
    val label: String,
    val version: String?,
    val date: String,
    val brokenFeatures: List<String>
)
