package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.Evaluation
import com.klee.sapio.domain.FetchIconUrlUseCase
import com.klee.sapio.domain.ListLatestEvaluationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var listLatestEvaluationsUseCase: ListLatestEvaluationsUseCase

    @Inject
    lateinit var fetchIconUrlUseCase: FetchIconUrlUseCase

    var evaluations = MutableLiveData<List<Evaluation>>()
    var iconUrl = MutableLiveData<String>()

    fun listEvaluations(onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val result = listLatestEvaluationsUseCase.invoke()
            evaluations.postValue(result)

            if (result.isEmpty()) {
                onError.invoke()
            } else {
                onSuccess.invoke()
            }
        }
    }

    fun fetchIconUrl(packageName: String) {
        viewModelScope.launch {
            iconUrl.postValue(fetchIconUrlUseCase.invoke(packageName))
        }
    }
}
