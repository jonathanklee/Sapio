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
class FeedViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var applicationRepository: RemoteEvaluationRepository

    var applications = MutableLiveData<List<RemoteEvaluation>>()

    fun listApplications() {
        viewModelScope.launch {
            val result = applicationRepository.getEvaluations()
            applications.postValue(result)
        }
    }
}
