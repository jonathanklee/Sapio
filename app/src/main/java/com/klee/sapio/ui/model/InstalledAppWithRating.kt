package com.klee.sapio.ui.model

import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.InstalledApplication

data class InstalledAppWithRating(
    val installedApp: InstalledApplication,
    val evaluation: Evaluation?
)
