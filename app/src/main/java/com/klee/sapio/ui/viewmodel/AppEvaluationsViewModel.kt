package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.system.GmsType
import com.klee.sapio.data.system.Settings
import com.klee.sapio.data.system.UserType
import com.klee.sapio.domain.FetchAppEvaluationUseCase
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
    private val fetchAppEvaluationUseCase: FetchAppEvaluationUseCase,
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
                        microgUser = fetchAppEvaluationUseCase(
                            packageName,
                            GmsType.MICROG,
                            UserType.SECURE
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
                        bareAospUser = fetchAppEvaluationUseCase(
                            packageName,
                            GmsType.BARE_AOSP,
                            UserType.SECURE
                        ).getOrNull(),
                        bareAospUserLoaded = true
                    )
                }
            }
        }

        loadRiskyEvaluationsIfEnabled(packageName)

        viewModelScope.launch {
            withContext(ioDispatcher) {
                _uiState.update {
                    it.copy(
                        iconUrl = fetchIconUrlUseCase(packageName).getOrDefault(""),
                        iconLoaded = true
                    )
                }
            }
        }
    }

    private fun loadRiskyEvaluationsIfEnabled(packageName: String) {
        if (!settings.isRootConfigurationEnabled()) return

        viewModelScope.launch {
            withContext(ioDispatcher) {
                _uiState.update {
                    it.copy(
                        microgRoot = fetchAppEvaluationUseCase(
                            packageName,
                            GmsType.MICROG,
                            UserType.RISKY
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
                        bareAospRoot = fetchAppEvaluationUseCase(
                            packageName,
                            GmsType.BARE_AOSP,
                            UserType.RISKY
                        ).getOrNull(),
                        bareAospRootLoaded = true
                    )
                }
            }
        }
    }
}
