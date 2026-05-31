package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.domain.SearchEvaluationUseCase
import com.klee.sapio.ui.state.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchEvaluationUseCase: SearchEvaluationUseCase
) : ViewModel() {

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun searchApplication(pattern: String, onError: () -> Unit) {
        searchJob?.cancel()
        _uiState.update { it.copy(query = pattern, isLoading = true, hasError = false) }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            performSearch(pattern, onError)
        }
    }

    private suspend fun performSearch(pattern: String, onError: () -> Unit) {
        val result = searchEvaluationUseCase(pattern)
        val list = result.getOrDefault(emptyList())
        val hasError = result.isFailure

        if (list.isEmpty() || hasError) {
            onError.invoke()
        }

        _uiState.update {
            it.copy(
                items = list,
                isLoading = false,
                hasError = hasError
            )
        }
    }
}
