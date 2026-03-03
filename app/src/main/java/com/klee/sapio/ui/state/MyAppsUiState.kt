package com.klee.sapio.ui.state

import com.klee.sapio.ui.model.InstalledAppWithRating

data class MyAppsUiState(
    val items: List<InstalledAppWithRating> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val progress: Int = 0
)
