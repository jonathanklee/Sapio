package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.SearchEvaluationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchEvaluationUseCase: SearchEvaluationUseCase
) : ViewModel() {

    val evaluations: Flow<List<Evaluation>>
        get() = _evaluations.asStateFlow()

    private var _evaluations: MutableStateFlow<List<Evaluation>> = MutableStateFlow(emptyList())

    suspend fun searchApplication(pattern: String, onError: () -> Unit) {
        val result = searchEvaluationUseCase.invoke(pattern)
        var hasError = false
        val list = result.getOrElse {
            hasError = true
            emptyList()
        }

        if (list.isEmpty() || hasError) {
            onError.invoke()
        }

        _evaluations.value = list
    }
}
