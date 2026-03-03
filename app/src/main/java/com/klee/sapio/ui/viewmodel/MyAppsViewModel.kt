package com.klee.sapio.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.data.local.DeviceAppDao
import com.klee.sapio.data.local.DeviceAppEntity
import com.klee.sapio.data.repository.InstalledApplicationsRepository
import com.klee.sapio.data.system.DeviceConfiguration
import com.klee.sapio.domain.CheckFdroidAvailabilityUseCase
import com.klee.sapio.domain.FetchAppEvaluationUseCase
import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.ui.model.InstalledAppWithRating
import com.klee.sapio.ui.state.MyAppsUiState
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
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@HiltViewModel
class MyAppsViewModel @Inject constructor(
    private val installedApplicationsRepository: InstalledApplicationsRepository,
    private val fetchAppEvaluationUseCase: FetchAppEvaluationUseCase,
    private val checkFdroidAvailabilityUseCase: CheckFdroidAvailabilityUseCase,
    private val deviceConfiguration: DeviceConfiguration,
    private val deviceAppDao: DeviceAppDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyAppsUiState())
    val uiState = _uiState.asStateFlow()

    private var lastLoadTime: Long? = null

    private fun isCacheValid(timestamp: Long?): Boolean {
        val last = timestamp ?: return false
        return System.currentTimeMillis() - last < CACHE_VALIDITY_MS
    }

    fun loadApps(forceRefresh: Boolean = false) {
        if (_uiState.value.isLoading || _uiState.value.isRefreshing) return
        if (!forceRefresh && _uiState.value.items.isNotEmpty() && isCacheValid(lastLoadTime)) return

        viewModelScope.launch {
            if (forceRefresh) {
                _uiState.update { it.copy(isLoading = true, isRefreshing = true, progress = 0) }
                fetchFromWebAndSave()
            } else {
                _uiState.update { it.copy(isLoading = true, progress = 0) }
                val entities = withContext(Dispatchers.IO) { deviceAppDao.getAll() }
                val lastCachedAt = entities.maxOfOrNull { it.cachedAt }

                if (entities.isNotEmpty() && isCacheValid(lastCachedAt)) {
                    val result = buildListFromEntities(entities)
                    lastLoadTime = lastCachedAt
                    _uiState.update { it.copy(items = result, isLoading = false) }
                } else {
                    fetchFromWebAndSave()
                }
            }
        }
    }

    private suspend fun buildListFromEntities(
        entities: List<DeviceAppEntity>
    ): List<InstalledAppWithRating> {
        val installedMap = withContext(Dispatchers.IO) {
            installedApplicationsRepository.getAppList(context).associateBy { it.packageName }
        }
        val total = entities.size
        val result = mutableListOf<InstalledAppWithRating>()
        entities.forEachIndexed { index, entity ->
            val installedApp = installedMap[entity.packageName] ?: return@forEachIndexed
            val evaluation = entity.rating?.let { rating ->
                Evaluation(
                    name = installedApp.name,
                    packageName = entity.packageName,
                    iconUrl = null,
                    rating = rating,
                    microg = 0,
                    secure = 0,
                    updatedAt = null,
                    createdAt = null,
                    publishedAt = null,
                    versionName = null
                )
            }
            result.add(InstalledAppWithRating(installedApp, evaluation))
            _uiState.update { it.copy(progress = ((index + 1) * 100) / total) }
        }
        return result
    }

    private suspend fun fetchFromWebAndSave() {
        val gmsType = deviceConfiguration.getGmsType()
        val userType = deviceConfiguration.isRisky()

        val installedApps = withContext(Dispatchers.IO) {
            installedApplicationsRepository.getAppList(context)
        }

        val total = installedApps.size
        val semaphore = Semaphore(PARALLEL_REQUESTS)
        val completed = AtomicInteger(0)

        val result = coroutineScope {
            installedApps.map { app ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        val isFdroid = checkFdroidAvailabilityUseCase(app.packageName)
                        val item = if (!isFdroid) {
                            val evaluation = fetchAppEvaluationUseCase(
                                app.packageName,
                                gmsType,
                                userType
                            ).getOrNull()
                            InstalledAppWithRating(app, evaluation)
                        } else {
                            null
                        }
                        _uiState.update { state ->
                            state.copy(progress = (completed.incrementAndGet() * 100) / total)
                        }
                        item
                    }
                }
            }.awaitAll().filterNotNull()
        }

        val now = System.currentTimeMillis()
        val entities = result.map { item ->
            DeviceAppEntity(
                packageName = item.installedApp.packageName,
                rating = item.evaluation?.rating,
                cachedAt = now
            )
        }

        withContext(Dispatchers.IO) {
            deviceAppDao.deleteAll()
            deviceAppDao.upsertAll(entities)
        }

        lastLoadTime = now
        _uiState.update { it.copy(items = result, isLoading = false, isRefreshing = false) }
    }

    companion object {
        private const val CACHE_VALIDITY_MS = 86_400_000L
        private const val PARALLEL_REQUESTS = 10
    }
}
