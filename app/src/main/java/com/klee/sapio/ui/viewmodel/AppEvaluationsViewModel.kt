package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.system.Settings
import com.klee.sapio.domain.FetchAppBareAospRiskyEvaluationUseCase
import com.klee.sapio.domain.FetchAppBareAospSecureEvaluationUseCase
import com.klee.sapio.domain.FetchAppMicrogRiskyEvaluationUseCase
import com.klee.sapio.domain.FetchAppMicrogSecureEvaluationUseCase
import com.klee.sapio.domain.FetchIconUrlUseCase
import com.klee.sapio.ui.state.AppEvaluationsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AppEvaluationsViewModel @Inject constructor(
    private val fetchAppMicrogSecureEvaluationUseCase: FetchAppMicrogSecureEvaluationUseCase,
    private val fetchAppMicrogRiskyEvaluationUseCase: FetchAppMicrogRiskyEvaluationUseCase,
    private val fetchAppBareAOspSecureEvaluationUseCase: FetchAppBareAospSecureEvaluationUseCase,
    private val fetchAppBareAospRiskyEvaluationUseCase: FetchAppBareAospRiskyEvaluationUseCase,
    private val fetchIconUrlUseCase: FetchIconUrlUseCase,
    private val settings: Settings
) : ViewModel() {

    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val _uiState = MutableStateFlow(AppEvaluationsUiState())
    val uiState = _uiState.asStateFlow()

    fun listEvaluations(packageName: String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                _uiState.update {
                    it.copy(
                        microgUser = fetchAppMicrogSecureEvaluationUseCase.invoke(
                            packageName
                        ).getOrNull(),
                        microgUserLoaded = true
                    )
                }
            }
        }

        viewModelScope.launch {
            withContext(ioDispatcher) {
                _uiState.update {
                    it.copy(
                        bareAospUser = fetchAppBareAOspSecureEvaluationUseCase.invoke(packageName).getOrNull(),
                        bareAospUserLoaded = true
                    )
                }
            }
        }

        if (settings.isRootConfigurationEnabled()) {
            viewModelScope.launch {
                withContext(ioDispatcher) {
                    _uiState.update {
                        it.copy(
                            microgRoot = fetchAppMicrogRiskyEvaluationUseCase.invoke(
                                packageName
                            ).getOrNull(),
                            microgRootLoaded = true
                        )
                    }
                }
            }

            viewModelScope.launch {
                withContext(ioDispatcher) {
                    _uiState.update {
                        it.copy(
                            bareAospRoot = fetchAppBareAospRiskyEvaluationUseCase.invoke(packageName).getOrNull(),
                            bareAospRootLoaded = true
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            withContext(ioDispatcher) {
                _uiState.update {
                    it.copy(
                        iconUrl = fetchIconUrlUseCase.invoke(packageName).getOrDefault(""),
                        iconLoaded = true
                    )
                }
            }
        }
    }
}
