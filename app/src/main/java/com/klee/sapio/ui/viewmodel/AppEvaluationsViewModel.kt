package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.Evaluation
import com.klee.sapio.domain.FetchAppBareAospRootEvaluationUseCase
import com.klee.sapio.domain.FetchAppBareAospUserEvaluationUseCase
import com.klee.sapio.domain.FetchAppMicrogRootEvaluationUseCase
import com.klee.sapio.domain.FetchAppMicrogUserEvaluationUseCase
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

    var microgUserEvaluation = MutableLiveData<Evaluation>()
    var microgRootEvaluation = MutableLiveData<Evaluation>()
    var bareAospUserEvaluation = MutableLiveData<Evaluation>()
    var bareAsopRootEvaluation = MutableLiveData<Evaluation>()

    fun listEvaluations(packageName: String) {
        viewModelScope.launch {
            microgUserEvaluation.postValue(fetchAppMicrogUserEvaluationUseCase.invoke(packageName))
            microgRootEvaluation.postValue(fetchAppMicrogRootEvaluationUseCase.invoke(packageName))
            bareAospUserEvaluation.postValue(fetchAppBareAOspUserEvaluationUseCase.invoke(packageName))
            bareAsopRootEvaluation.postValue(fetchAppBareAospRootEvaluationUseCase.invoke(packageName))
        }
    }
}
