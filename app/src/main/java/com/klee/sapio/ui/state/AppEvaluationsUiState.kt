package com.klee.sapio.ui.state

import com.klee.sapio.domain.model.Evaluation

data class AppEvaluationsUiState(
    val microgUser: Evaluation? = null,
    val microgRoot: Evaluation? = null,
    val bareAospUser: Evaluation? = null,
    val bareAospRoot: Evaluation? = null,
    val iconUrl: String = "",
    val pendingCount: Int = 0,
    val hasError: Boolean = false
) {
    val isFullyLoaded: Boolean get() = pendingCount == 0
}
