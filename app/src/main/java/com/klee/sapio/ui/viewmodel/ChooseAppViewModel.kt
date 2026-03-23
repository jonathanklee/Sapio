package com.klee.sapio.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.repository.InstalledApplicationsRepository
import com.klee.sapio.domain.CheckFdroidAvailabilityUseCase
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.ui.state.ChooseAppUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChooseAppViewModel @Inject constructor(
    private val installedApplicationsRepository: InstalledApplicationsRepository,
    private val checkFdroidAvailabilityUseCase: CheckFdroidAvailabilityUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChooseAppUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            val allApps = withContext(Dispatchers.IO) {
                installedApplicationsRepository.getAppList(context)
            }
            val filtered = filterFdroidApps(allApps)
            _uiState.update { it.copy(apps = filtered, isLoading = false) }
        }
    }

    private suspend fun filterFdroidApps(apps: List<InstalledApplication>): List<InstalledApplication> {
        val semaphore = Semaphore(PARALLEL_REQUESTS)
        return coroutineScope {
            apps.map { app ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        if (checkFdroidAvailabilityUseCase(app.packageName)) null else app
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    companion object {
        private const val PARALLEL_REQUESTS = 10
    }
}
