package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.EvaluationRepositoryStrapi
import com.klee.sapio.domain.ListAllEvaluationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var applicationRepository: EvaluationRepositoryStrapi

    @Inject
    lateinit var listAllEvaluationUseCase: ListAllEvaluationUseCase

    var evaluations = MutableLiveData<List<Evaluation>>()

    fun listEvaluations(onError: () -> Unit) {
        viewModelScope.launch {
            val result = listAllEvaluationUseCase.invoke()
            evaluations.postValue(result)

            if (result.isEmpty()) {
                onError.invoke()
            }
        }
    }
}