package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.domain.ListLatestEvaluationsUseCase
import com.klee.sapio.ui.state.FeedUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val listLatestEvaluationsUseCase: ListLatestEvaluationsUseCase
) : ViewModel() {

    companion object {
        const val NUMBER_OF_PAGES = 10
    }

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(items = emptyList(), isLoading = true, hasError = false) }
            for (i in 1..NUMBER_OF_PAGES) {
                val result = listLatestEvaluationsUseCase(i)
                if (result.isFailure) {
                    _uiState.update { it.copy(isLoading = false, hasError = true) }
                    return@launch
                }

                val current = _uiState.value.items
                val next = current + result.getOrDefault(emptyList())
                _uiState.update { it.copy(items = next, isLoading = false) }
            }
        }
    }
}
