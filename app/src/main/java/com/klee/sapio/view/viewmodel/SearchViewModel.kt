package com.klee.sapio.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.model.RemoteEvaluation
import com.klee.sapio.model.RemoteEvaluationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var applicationRepository: RemoteEvaluationRepository

    val foundApplications = MutableLiveData<List<RemoteEvaluation>>()

    fun searchApplication(pattern: String) {
        viewModelScope.launch {
            val result = applicationRepository.searchApplicationsFromStrapi(pattern)
            foundApplications.postValue(result)
        }
    }
}
