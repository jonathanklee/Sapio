package com.klee.sapio.ui.state

import com.klee.sapio.domain.model.Evaluation

data class AppEvaluationsUiState(
    val microgUser: Evaluation? = null,
    val microgRoot: Evaluation? = null,
    val bareAospUser: Evaluation? = null,
    val bareAospRoot: Evaluation? = null,
    val iconUrl: String = "",
    val microgUserLoaded: Boolean = false,
    val microgRootLoaded: Boolean = false,
    val bareAospUserLoaded: Boolean = false,
    val bareAospRootLoaded: Boolean = false,
    val iconLoaded: Boolean = false,
    val hasError: Boolean = false
)
