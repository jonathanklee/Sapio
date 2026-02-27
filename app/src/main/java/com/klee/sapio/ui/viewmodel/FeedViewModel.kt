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

    private var currentPage = 0
    private var hasMorePages = true

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            currentPage = 0
            hasMorePages = true
            _uiState.update { it.copy(items = emptyList(), isLoading = true, isLoadingMore = false, hasError = false) }
            loadPage()
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !hasMorePages) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            loadPage()
        }
    }

    private suspend fun loadPage() {
        currentPage++
        val result = listLatestEvaluationsUseCase(currentPage)
        if (result.isFailure) {
            _uiState.update { it.copy(isLoading = false, isLoadingMore = false, hasError = true) }
            return
        }

        val newItems = result.getOrDefault(emptyList())
        _uiState.update {
            val combined = (it.items + newItems).distinctBy { item -> item.packageName }
            hasMorePages = combined.size > it.items.size
            it.copy(
                items = combined,
                isLoading = false,
                isLoadingMore = false
            )
        }
    }
}
