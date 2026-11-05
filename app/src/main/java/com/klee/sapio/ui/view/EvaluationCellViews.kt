package com.klee.sapio.ui.view

import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.chip.ChipGroup

class EvaluationCellViews(
    val icon: ImageView,
    val rating: TextView,
    val version: TextView,
    val date: TextView,
    val brokenFeatures: ChipGroup
)
