package com.klee.sapio.ui.viewmodel

import com.klee.sapio.data.system.DeviceConfiguration
import com.klee.sapio.ui.state.EvaluateUiState
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class EvaluateViewModel @Inject constructor(
    deviceConfiguration: DeviceConfiguration
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EvaluateUiState(
            gmsType = deviceConfiguration.getGmsType(),
            userType = deviceConfiguration.isUnsafe()
        )
    )
    val uiState = _uiState.asStateFlow()
}
