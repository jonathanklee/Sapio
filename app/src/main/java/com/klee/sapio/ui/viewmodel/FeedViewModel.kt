package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.klee.sapio.data.Evaluation
import com.klee.sapio.domain.FetchIconUrlUseCase
import com.klee.sapio.domain.ListLatestEvaluationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor() : ViewModel() {

    companion object {
        const val NUMBER_OF_PAGES = 10
    }

    @Inject
    lateinit var listLatestEvaluationsUseCase: ListLatestEvaluationsUseCase

    @Inject
    lateinit var fetchIconUrlUseCase: FetchIconUrlUseCase

    val evaluations: Flow<List<Evaluation>> = flow {
        for (i in 1..NUMBER_OF_PAGES) {
            emit(listLatestEvaluationsUseCase.invoke(i))
        }
    }
}
