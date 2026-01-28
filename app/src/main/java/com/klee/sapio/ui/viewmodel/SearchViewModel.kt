package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.domain.SearchEvaluationUseCase
import com.klee.sapio.ui.state.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchEvaluationUseCase: SearchEvaluationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    fun searchApplication(pattern: String, onError: () -> Unit) {
        _uiState.update { it.copy(query = pattern, isLoading = true, hasError = false) }
        viewModelScope.launch {
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
}
