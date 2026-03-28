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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    private var loadingJob: Job? = null

    companion object {
        private const val FETCHES_WITH_UNSAFE = 5
        private const val FETCHES_WITHOUT_UNSAFE = 3
    }

    fun listEvaluations(packageName: String) {
        loadingJob?.cancel()
        _uiState.value = AppEvaluationsUiState()

        val expectedFetches = if (settings.isUnsafeConfigurationEnabled()) FETCHES_WITH_UNSAFE else FETCHES_WITHOUT_UNSAFE
        _uiState.update { it.copy(pendingCount = expectedFetches) }

        loadingJob = viewModelScope.launch {
            launch(ioDispatcher) {
                _uiState.update {
                    it.copy(
                        microgUser = fetchAppEvaluationUseCase(
                            packageName,
                            GmsType.MICROG,
                            UserType.SECURE
                        ).getOrNull(),
                        pendingCount = it.pendingCount - 1
                    )
                }
            }

            launch(ioDispatcher) {
                _uiState.update {
                    it.copy(
                        bareAospUser = fetchAppEvaluationUseCase(
                            packageName,
                            GmsType.BARE_AOSP,
                            UserType.SECURE
                        ).getOrNull(),
                        pendingCount = it.pendingCount - 1
                    )
                }
            }

            if (settings.isUnsafeConfigurationEnabled()) {
                launch(ioDispatcher) {
                    _uiState.update {
                        it.copy(
                            microgRoot = fetchAppEvaluationUseCase(
                                packageName,
                                GmsType.MICROG,
                                UserType.UNSAFE
                            ).getOrNull(),
                            pendingCount = it.pendingCount - 1
                        )
                    }
                }

                launch(ioDispatcher) {
                    _uiState.update {
                        it.copy(
                            bareAospRoot = fetchAppEvaluationUseCase(
                                packageName,
                                GmsType.BARE_AOSP,
                                UserType.UNSAFE
                            ).getOrNull(),
                            pendingCount = it.pendingCount - 1
                        )
                    }
                }
            }

            launch(ioDispatcher) {
                _uiState.update {
                    it.copy(iconUrl = fetchIconUrlUseCase(packageName).getOrDefault(""))
                }
            }
        }
    }

    fun onIconDisplayed() {
        _uiState.update { it.copy(pendingCount = it.pendingCount - 1) }
    }
}
