package com.klee.sapio.ui.state

import com.klee.sapio.domain.model.EvaluationHistory

data class AppEvaluationsUiState(
    val microgUser: EvaluationHistory? = null,
    val microgRoot: EvaluationHistory? = null,
    val bareAospUser: EvaluationHistory? = null,
    val bareAospRoot: EvaluationHistory? = null,
    val iconUrl: String? = null,
    val pendingCount: Int = 0,
    val evaluationsLoaded: Boolean = false,
    val hasError: Boolean = false
) {
    val isFullyLoaded: Boolean get() = pendingCount == 0
}
