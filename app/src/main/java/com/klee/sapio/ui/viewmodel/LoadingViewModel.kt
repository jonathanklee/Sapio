package com.klee.sapio.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.repository.InstalledApplicationsRepository
import com.klee.sapio.domain.EvaluateAppUseCase
import com.klee.sapio.ui.state.EvaluateEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoadingViewModel @Inject constructor(
    private val installedApplicationsRepository: InstalledApplicationsRepository,
    private val evaluateAppUseCase: EvaluateAppUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _events = MutableSharedFlow<EvaluateEvent>()
    val events = _events.asSharedFlow()

    fun submit(packageName: String, appName: String, rating: Int) {
        viewModelScope.launch {
            val app = installedApplicationsRepository.getApplicationFromPackageName(context, packageName)
            if (app == null) {
                _events.emit(EvaluateEvent.ShowError)
                return@launch
            }
            val result = evaluateAppUseCase(app, rating)
            if (result.isSuccess) {
                _events.emit(EvaluateEvent.NavigateToSuccess(packageName, appName))
            } else {
                _events.emit(EvaluateEvent.ShowError)
            }
        }
    }
}
