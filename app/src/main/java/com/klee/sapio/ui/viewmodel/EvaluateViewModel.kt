package com.klee.sapio.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.repository.InstalledApplicationsRepository
import com.klee.sapio.data.system.DeviceConfiguration
import com.klee.sapio.domain.EvaluateAppUseCase
import com.klee.sapio.ui.state.EvaluateEvent
import com.klee.sapio.ui.state.EvaluateUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EvaluateViewModel @Inject constructor(
    private val installedApplicationsRepository: InstalledApplicationsRepository,
    private val evaluateAppUseCase: EvaluateAppUseCase,
    deviceConfiguration: DeviceConfiguration,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EvaluateUiState(
            gmsType = deviceConfiguration.getGmsType(),
            userType = deviceConfiguration.isRisky()
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EvaluateEvent>()
    val events = _events.asSharedFlow()

    fun submit(packageName: String, appName: String, rating: Int) {
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val app = installedApplicationsRepository.getApplicationFromPackageName(context, packageName)
            if (app == null) {
                _uiState.update { it.copy(isSubmitting = false) }
                return@launch
            }
            val result = evaluateAppUseCase(app, rating)
            _uiState.update { it.copy(isSubmitting = false) }
            if (result.isSuccess) {
                _events.emit(EvaluateEvent.NavigateToSuccess(packageName, appName))
            } else {
                _events.emit(EvaluateEvent.ShowError)
            }
        }
    }
}
