package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.ListLatestEvaluationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val listLatestEvaluationsUseCase: ListLatestEvaluationsUseCase
) : ViewModel() {

    companion object {
        const val NUMBER_OF_PAGES = 10
    }

    val evaluations: Flow<List<Evaluation>> = flow {
        for (i in 1..NUMBER_OF_PAGES) {
            val result = listLatestEvaluationsUseCase.invoke(i)
            emit(result.getOrDefault(emptyList()))
        }
    }
}
