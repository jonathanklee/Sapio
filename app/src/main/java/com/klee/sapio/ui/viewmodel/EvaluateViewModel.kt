package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.klee.sapio.domain.DeviceInfo
import com.klee.sapio.ui.state.EvaluateUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class EvaluateViewModel @Inject constructor(
    deviceInfo: DeviceInfo
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EvaluateUiState(
            gmsType = deviceInfo.getGmsType(),
            userType = deviceInfo.isUnsafe()
        )
    )
    val uiState = _uiState.asStateFlow()
}
