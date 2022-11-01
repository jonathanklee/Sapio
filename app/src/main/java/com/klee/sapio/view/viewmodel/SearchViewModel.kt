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
class SearchViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var applicationRepository: EvaluationRepository

    val foundApplications = MutableLiveData<List<Evaluation>>()

    fun searchApplication(pattern: String) {
        viewModelScope.launch {
            val result = applicationRepository.searchEvaluations(pattern)
            foundApplications.postValue(result)
        }
    }
}
