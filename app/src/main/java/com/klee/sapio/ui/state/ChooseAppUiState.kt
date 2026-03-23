package com.klee.sapio.ui.state

import com.klee.sapio.domain.model.InstalledApplication

data class ChooseAppUiState(
    val apps: List<InstalledApplication> = emptyList(),
    val isLoading: Boolean = true
)
