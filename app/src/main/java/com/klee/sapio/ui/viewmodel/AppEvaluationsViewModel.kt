package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.Settings
import com.klee.sapio.domain.FetchAppBareAospRiskyEvaluationUseCase
import com.klee.sapio.domain.FetchAppBareAospSecureEvaluationUseCase
import com.klee.sapio.domain.FetchAppMicrogRiskyEvaluationUseCase
import com.klee.sapio.domain.FetchAppMicrogSecureEvaluationUseCase
import com.klee.sapio.domain.FetchIconUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AppEvaluationsViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var fetchAppMicrogSecureEvaluationUseCase: FetchAppMicrogSecureEvaluationUseCase

    @Inject
    lateinit var fetchAppMicrogRiskyEvaluationUseCase: FetchAppMicrogRiskyEvaluationUseCase

    @Inject
    lateinit var fetchAppBareAOspSecureEvaluationUseCase: FetchAppBareAospSecureEvaluationUseCase

    @Inject
    lateinit var fetchAppBareAospRiskyEvaluationUseCase: FetchAppBareAospRiskyEvaluationUseCase

    @Inject
    lateinit var fetchIconUrlUseCase: FetchIconUrlUseCase

    @Inject
    lateinit var settings: Settings

    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    var microgUserEvaluation = MutableLiveData<Evaluation>()
    var microgRootEvaluation = MutableLiveData<Evaluation>()
    var bareAospUserEvaluation = MutableLiveData<Evaluation>()
    var bareAsopRootEvaluation = MutableLiveData<Evaluation>()
    var iconUrl = MutableLiveData<String>()

    fun listEvaluations(packageName: String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                microgUserEvaluation.postValue(
                    fetchAppMicrogSecureEvaluationUseCase.invoke(
                        packageName
                    )
                )
            }
        }

        viewModelScope.launch {
            withContext(ioDispatcher) {
                bareAospUserEvaluation.postValue(
                    fetchAppBareAOspSecureEvaluationUseCase.invoke(packageName)
                )
            }
        }

        if (settings.isRootConfigurationEnabled()) {
            viewModelScope.launch {
                withContext(ioDispatcher) {
                    microgRootEvaluation.postValue(
                        fetchAppMicrogRiskyEvaluationUseCase.invoke(
                            packageName
                        )
                    )
                }
            }

            viewModelScope.launch {
                withContext(ioDispatcher) {
                    bareAsopRootEvaluation.postValue(
                        fetchAppBareAospRiskyEvaluationUseCase.invoke(packageName)
                    )
                }
            }
        }

        viewModelScope.launch {
            withContext(ioDispatcher) {
                iconUrl.postValue(fetchIconUrlUseCase.invoke(packageName))
            }
        }
    }
}
