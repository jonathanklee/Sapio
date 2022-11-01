package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.EvaluationRepository
import com.klee.sapio.domain.ListAllEvaluationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(): ViewModel() {

    @Inject
    lateinit var applicationRepository: EvaluationRepository

    @Inject
    lateinit var listAllEvaluationUseCase: ListAllEvaluationUseCase

    var evaluations = MutableLiveData<List<Evaluation>>()

    fun listEvaluations() {
        viewModelScope.launch {
            val result = listAllEvaluationUseCase.invoke()
            evaluations.postValue(result)
        }
    }
}
