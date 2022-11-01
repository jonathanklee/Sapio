package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.EvaluationRepositoryStrapi
import com.klee.sapio.domain.SearchEvaluationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(): ViewModel() {

    @Inject
    lateinit var searchEvaluationUseCase: SearchEvaluationUseCase

    @Inject
    lateinit var applicationRepository: EvaluationRepositoryStrapi

    var foundEvaluations = MutableLiveData<List<Evaluation>>()

    fun searchApplication(pattern: String) {
        viewModelScope.launch {
            val result = searchEvaluationUseCase.invoke(pattern)
            foundEvaluations.postValue(result)
        }
    }
}