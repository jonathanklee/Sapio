package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.domain.AppSettings
import com.klee.sapio.domain.FetchEvaluationHistoryUseCase
import com.klee.sapio.domain.FetchIconUrlUseCase
import com.klee.sapio.domain.model.Environment
import com.klee.sapio.domain.model.EvaluationHistory
import com.klee.sapio.domain.model.GmsType
import com.klee.sapio.domain.model.UserType
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
    private val fetchEvaluationHistoryUseCase: FetchEvaluationHistoryUseCase,
    private val fetchIconUrlUseCase: FetchIconUrlUseCase,
    private val settings: AppSettings
) : ViewModel() {

    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val _uiState = MutableStateFlow(AppEvaluationsUiState())
    val uiState = _uiState.asStateFlow()

    private var loadingJob: Job? = null

    companion object {
        private const val PENDING_EVALUATIONS = 1
        private const val PENDING_ICON = 1
    }

    fun listEvaluations(packageName: String) {
        loadingJob?.cancel()
        _uiState.value = AppEvaluationsUiState(pendingCount = PENDING_EVALUATIONS + PENDING_ICON)

        loadingJob = viewModelScope.launch {
            launch(ioDispatcher) {
                _uiState.update {
                    it.copy(iconUrl = fetchIconUrlUseCase(packageName).getOrDefault(""))
                }
            }

            launch(ioDispatcher) {
                val histories = fetchEvaluationHistoryUseCase(packageName)
                _uiState.update { it.applyHistories(histories.getOrNull(), histories.isFailure) }
            }
        }
    }

    private fun AppEvaluationsUiState.applyHistories(
        histories: Map<Environment, EvaluationHistory>?,
        hasError: Boolean
    ) = copy(
        microgUser = histories?.get(Environment(GmsType.MICROG, UserType.STANDARD)),
        microgRoot = permissiveOrNull(histories, GmsType.MICROG),
        bareAospUser = histories?.get(Environment(GmsType.BARE_AOSP, UserType.STANDARD)),
        bareAospRoot = permissiveOrNull(histories, GmsType.BARE_AOSP),
        pendingCount = pendingCount - PENDING_EVALUATIONS,
        evaluationsLoaded = true,
        hasError = hasError
    )

    private fun permissiveOrNull(
        histories: Map<Environment, EvaluationHistory>?,
        gmsType: Int
    ): EvaluationHistory? {
        if (!settings.isUnsafeConfigurationEnabled()) {
            return null
        }

        return histories?.get(Environment(gmsType, UserType.PERMISSIVE))
    }

    fun onIconDisplayed() {
        _uiState.update { it.copy(pendingCount = it.pendingCount - PENDING_ICON) }
    }
}
