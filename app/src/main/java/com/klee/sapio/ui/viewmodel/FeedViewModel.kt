package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.Evaluation
import com.klee.sapio.domain.FetchIconUrlUseCase
import com.klee.sapio.domain.ListLatestEvaluationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var listLatestEvaluationsUseCase: ListLatestEvaluationsUseCase

    @Inject
    lateinit var fetchIconUrlUseCase: FetchIconUrlUseCase

    val evaluations: Flow<List<Evaluation>>
        get() = _evaluations.asStateFlow()

    private val _evaluations: MutableStateFlow<List<Evaluation>> = MutableStateFlow(emptyList())

    fun listEvaluation(onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val list = listLatestEvaluationsUseCase.invoke()
            _evaluations.value = list
            if (list.isEmpty()) {
                onError.invoke()
            } else {
                onSuccess.invoke()
            }
        }
    }
}
