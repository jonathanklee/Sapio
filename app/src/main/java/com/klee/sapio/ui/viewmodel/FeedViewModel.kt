package com.klee.sapio.ui.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.EvaluationRepositoryStrapi
import com.klee.sapio.domain.ListAllEvaluationUseCase
import com.klee.sapio.ui.view.ToastMessage
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

    fun listEvaluations(context: Context) {
        viewModelScope.launch {
            val result = listAllEvaluationUseCase.invoke()
            evaluations.postValue(result)

            if (result.isEmpty()) {
                ToastMessage.showNetworkIssue(context)
            }
        }
    }
}
