package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.domain.EvaluateAppUseCase
import com.klee.sapio.domain.InstalledApplicationsDataSource
import com.klee.sapio.ui.state.EvaluateEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoadingViewModel @Inject constructor(
    private val installedApplicationsDataSource: InstalledApplicationsDataSource,
    private val evaluateAppUseCase: EvaluateAppUseCase
) : ViewModel() {

    private val _events = MutableSharedFlow<EvaluateEvent>()
    val events = _events.asSharedFlow()

    fun submit(packageName: String, appName: String, rating: Int, brokenFeatures: List<String>?) {
        viewModelScope.launch {
            val app = installedApplicationsDataSource.getInstalledApplication(packageName)
            if (app == null) {
                _events.emit(EvaluateEvent.ShowError)
                return@launch
            }
            val result = evaluateAppUseCase(app, rating, brokenFeatures)
            if (result.isSuccess) {
                _events.emit(EvaluateEvent.NavigateToSuccess(packageName, appName))
            } else {
                _events.emit(EvaluateEvent.ShowError)
            }
        }
    }
}
