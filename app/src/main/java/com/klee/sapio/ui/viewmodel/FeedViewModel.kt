package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.Evaluation
import com.klee.sapio.domain.FetchIconUrlUseCase
import com.klee.sapio.domain.ListLatestEvaluationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var listLatestEvaluationsUseCase: ListLatestEvaluationsUseCase

    @Inject
    lateinit var fetchIconUrlUseCase: FetchIconUrlUseCase

    var evaluations: Flow<List<Evaluation>> = MutableStateFlow(emptyList())

    var iconUrl = MutableLiveData<String>()

    fun listEvaluation(onSuccess: () -> Unit, onError: () -> Unit): Flow<List<Evaluation>> = flow {
        val list = listLatestEvaluationsUseCase.invoke()
        if (list.isEmpty()) {
            onError.invoke()
        } else {
            onSuccess.invoke()
        }
        emit(list)
    }
    fun fetchIconUrl(packageName: String) {
        viewModelScope.launch {
            iconUrl.postValue(fetchIconUrlUseCase.invoke(packageName))
        }
    }
}
