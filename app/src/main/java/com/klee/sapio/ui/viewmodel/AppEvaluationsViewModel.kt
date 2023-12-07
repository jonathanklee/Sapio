package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.Evaluation
import com.klee.sapio.domain.FetchAppBareAospRootEvaluationUseCase
import com.klee.sapio.domain.FetchAppBareAospUserEvaluationUseCase
import com.klee.sapio.domain.FetchAppMicrogRootEvaluationUseCase
import com.klee.sapio.domain.FetchAppMicrogUserEvaluationUseCase
import com.klee.sapio.domain.FetchIconUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppEvaluationsViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var fetchAppMicrogUserEvaluationUseCase: FetchAppMicrogUserEvaluationUseCase
    @Inject
    lateinit var fetchAppMicrogRootEvaluationUseCase: FetchAppMicrogRootEvaluationUseCase
    @Inject
    lateinit var fetchAppBareAOspUserEvaluationUseCase: FetchAppBareAospUserEvaluationUseCase
    @Inject
    lateinit var fetchAppBareAospRootEvaluationUseCase: FetchAppBareAospRootEvaluationUseCase
    @Inject
    lateinit var fetchIconUrlUseCase: FetchIconUrlUseCase

    var microgUserEvaluation = MutableLiveData<Evaluation>()
    var microgRootEvaluation = MutableLiveData<Evaluation>()
    var bareAospUserEvaluation = MutableLiveData<Evaluation>()
    var bareAsopRootEvaluation = MutableLiveData<Evaluation>()
    var iconUrl = MutableLiveData<String>()

    fun listEvaluations(packageName: String) {
        viewModelScope.launch {
            microgUserEvaluation.postValue(fetchAppMicrogUserEvaluationUseCase.invoke(packageName))
        }

        viewModelScope.launch {
            microgRootEvaluation.postValue(fetchAppMicrogRootEvaluationUseCase.invoke(packageName))
        }

        viewModelScope.launch {
            bareAospUserEvaluation.postValue(
                fetchAppBareAOspUserEvaluationUseCase.invoke(packageName)
            )
        }

        viewModelScope.launch {
            bareAsopRootEvaluation.postValue(
                fetchAppBareAospRootEvaluationUseCase.invoke(packageName)
            )
        }

        viewModelScope.launch {
            iconUrl.postValue(fetchIconUrlUseCase.invoke(packageName))
        }
    }
}
