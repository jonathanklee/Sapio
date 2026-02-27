package com.klee.sapio.ui.state

import com.klee.sapio.domain.model.Evaluation

data class FeedUiState(
    val items: List<Evaluation> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasError: Boolean = false
)
