package com.klee.sapio.ui.state

data class EvaluateUiState(
    val isSubmitting: Boolean = false,
    val gmsType: Int,
    val userType: Int
)

sealed class EvaluateEvent {
    data class NavigateToSuccess(val packageName: String, val appName: String) : EvaluateEvent()
    object ShowError : EvaluateEvent()
}
