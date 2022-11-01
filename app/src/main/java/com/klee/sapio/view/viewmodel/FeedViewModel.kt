package com.klee.sapio.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.model.Evaluation
import com.klee.sapio.model.EvaluationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var applicationRepository: EvaluationRepository

    var evaluations = MutableLiveData<List<Evaluation>>()

    fun listEvaluations() {
        viewModelScope.launch {
            val result = applicationRepository.getEvaluations()
            evaluations.postValue(result)
        }
    }

    fun isEvaluationAvailable(): Boolean {
        return applicationRepository.isAvailable()
    }
}
