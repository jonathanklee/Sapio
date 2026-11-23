package com.klee.sapio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.domain.EvaluateAppUseCase
import com.klee.sapio.domain.InstalledApplicationsDataSource
import com.klee.sapio.ui.state.EvaluateEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoadingViewModel @Inject constructor(
    private val installedApplicationsDataSource: InstalledApplicationsDataSource,
    private val evaluateAppUseCase: EvaluateAppUseCase
) : ViewModel() {

    private val _events = Channel<EvaluateEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var submitJob: Job? = null

    fun submit(packageName: String, appName: String, rating: Int, brokenFeatures: List<String>?) {
        if (submitJob != null) {
            return
        }

        submitJob = viewModelScope.launch {
            val app = installedApplicationsDataSource.getInstalledApplication(packageName)
            if (app == null) {
                _events.send(EvaluateEvent.ShowError)
                return@launch
            }

            val result = evaluateAppUseCase(app, rating, brokenFeatures)
            if (result.isSuccess) {
                _events.send(EvaluateEvent.NavigateToSuccess(packageName, appName))
            } else {
                _events.send(EvaluateEvent.ShowError)
            }
        }
    }
}
