package com.klee.sapio

import com.klee.sapio.domain.DeviceAppCacheRepository
import com.klee.sapio.domain.DeviceInfo
import com.klee.sapio.domain.EvaluationRepository
import com.klee.sapio.domain.FetchAppEvaluationUseCase
import com.klee.sapio.domain.InstalledApplicationsDataSource
import com.klee.sapio.domain.model.CachedDeviceApp
import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.GmsType
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.domain.model.UploadEvaluation
import com.klee.sapio.domain.model.UserType
import com.klee.sapio.ui.viewmodel.MyAppsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyAppsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadApps reads a fresh cache without hitting the network`() = runTest(dispatcher) {
        val cache = FakeCache(listOf(cached("pkg.one", rating = 1, cachedAt = System.currentTimeMillis())))
        val evaluations = FakeEvaluations()
        val vm = buildViewModel(cache = cache, evaluations = evaluations)

        vm.loadApps()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(1, state.items.single().evaluation?.rating)
        assertFalse(state.isLoading)
        assertEquals(0, evaluations.calls)
    }

    @Test
    fun `loadApps drops cached entries for applications no longer installed`() = runTest(dispatcher) {
        val cache = FakeCache(
            listOf(
                cached("pkg.one", rating = 1, cachedAt = System.currentTimeMillis()),
                cached("pkg.gone", rating = 2, cachedAt = System.currentTimeMillis())
            )
        )
        val vm = buildViewModel(cache = cache)

        vm.loadApps()
        advanceUntilIdle()

        assertEquals(listOf("pkg.one"), vm.uiState.value.items.map { it.installedApp.packageName })
    }

    @Test
    fun `loadApps keeps a cached application that has never been evaluated`() = runTest(dispatcher) {
        val cache = FakeCache(listOf(cached("pkg.one", rating = null, cachedAt = System.currentTimeMillis())))
        val vm = buildViewModel(cache = cache)

        vm.loadApps()
        advanceUntilIdle()

        assertNull(vm.uiState.value.items.single().evaluation)
    }

    @Test
    fun `loadApps falls back to the network when the cache is stale`() = runTest(dispatcher) {
        val cache = FakeCache(listOf(cached("pkg.one", rating = 1, cachedAt = STALE_TIMESTAMP)))
        val evaluations = FakeEvaluations(rating = 3)
        val vm = buildViewModel(cache = cache, evaluations = evaluations)

        vm.loadApps()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(3, state.items.single().evaluation?.rating)
        assertEquals(1, evaluations.calls)
        assertEquals(100, state.progress)
        assertEquals(listOf("pkg.one"), cache.saved.map { it.packageName })
    }

    @Test
    fun `loadApps fetches from the network when the cache is empty`() = runTest(dispatcher) {
        val cache = FakeCache(emptyList())
        val evaluations = FakeEvaluations(rating = 2)
        val vm = buildViewModel(cache = cache, evaluations = evaluations)

        vm.loadApps()
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.items.single().evaluation?.rating)
        assertEquals(1, evaluations.calls)
    }

    @Test
    fun `loadApps ignores a second call while the cache is still fresh`() = runTest(dispatcher) {
        val cache = FakeCache(listOf(cached("pkg.one", rating = 1, cachedAt = System.currentTimeMillis())))
        val vm = buildViewModel(cache = cache)

        vm.loadApps()
        advanceUntilIdle()
        cache.reads = 0

        vm.loadApps()
        advanceUntilIdle()

        assertEquals(0, cache.reads)
    }

    @Test
    fun `forced refresh goes to the network and clears the refreshing flag`() = runTest(dispatcher) {
        val cache = FakeCache(listOf(cached("pkg.one", rating = 1, cachedAt = System.currentTimeMillis())))
        val evaluations = FakeEvaluations(rating = 3)
        val vm = buildViewModel(cache = cache, evaluations = evaluations)

        vm.loadApps(forceRefresh = true)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(3, state.items.single().evaluation?.rating)
        assertEquals(1, evaluations.calls)
        assertFalse(state.isRefreshing)
        assertTrue(cache.saved.isNotEmpty())
    }

    @Test
    fun `an unevaluated application is reported without a rating`() = runTest(dispatcher) {
        val vm = buildViewModel(cache = FakeCache(emptyList()), evaluations = FakeEvaluations(rating = null))

        vm.loadApps()
        advanceUntilIdle()

        assertNull(vm.uiState.value.items.single().evaluation)
        assertNull(vm.uiState.value.items.single().evaluation?.rating)
    }

    private fun buildViewModel(
        cache: FakeCache,
        evaluations: FakeEvaluations = FakeEvaluations()
    ): MyAppsViewModel {
        return MyAppsViewModel(
            FakeInstalledApplications(),
            evaluations,
            FakeDeviceInfo(),
            cache
        ).apply {
            ioDispatcher = dispatcher
        }
    }

    private fun cached(packageName: String, rating: Int?, cachedAt: Long) =
        CachedDeviceApp(packageName, rating, cachedAt)

    private class FakeInstalledApplications : InstalledApplicationsDataSource {
        override fun listInstalledApplications() =
            listOf(InstalledApplication("One", "pkg.one", "1.0"))

        override fun getInstalledApplication(packageName: String) =
            listInstalledApplications().firstOrNull { it.packageName == packageName }
    }

    private class FakeDeviceInfo : DeviceInfo {
        override fun getGmsType() = GmsType.MICROG
        override fun isPermissive() = UserType.STANDARD
    }

    private class FakeCache(initial: List<CachedDeviceApp>) : DeviceAppCacheRepository {
        private val entries = initial
        var saved: List<CachedDeviceApp> = emptyList()
        var reads = 0

        override suspend fun getAll(): List<CachedDeviceApp> {
            reads++
            return entries
        }

        override suspend fun replaceAll(items: List<CachedDeviceApp>) {
            saved = items
        }
    }

    private class FakeEvaluations(
        private val rating: Int? = 1
    ) : FetchAppEvaluationUseCase(NoopRepository()) {
        var calls = 0

        override suspend fun invoke(
            packageName: String,
            gmsType: Int,
            userType: Int
        ): Result<Evaluation?> {
            calls++
            val evaluation = rating?.let {
                Evaluation(
                    name = packageName,
                    packageName = packageName,
                    iconUrl = null,
                    rating = it,
                    microg = gmsType,
                    secure = userType,
                    updatedAt = null,
                    createdAt = null,
                    publishedAt = null,
                    versionName = null,
                    brokenFeatures = null
                )
            }
            return Result.success(evaluation)
        }
    }

    private class NoopRepository : EvaluationRepository {
        override suspend fun listLatestEvaluations(pageNumber: Int) = Result.success(emptyList<Evaluation>())
        override suspend fun searchEvaluations(pattern: String) = Result.success(emptyList<Evaluation>())
        override suspend fun addEvaluation(evaluation: UploadEvaluation) = Result.success(Unit)
        override suspend fun fetchEvaluation(appPackageName: String, gmsType: Int, userType: Int) =
            Result.success(null)
        override suspend fun fetchEvaluationHistory(packageName: String) = Result.success(emptyList<Evaluation>())
        override suspend fun uploadIcon(packageName: String) = Result.success(emptyList<Icon>())
        override suspend fun existingIcon(iconName: String) = Result.success(emptyList<Icon>())
        override suspend fun deleteIcon(id: Int) = Result.success(Unit)
    }

    private companion object {
        const val STALE_TIMESTAMP = 1_000L
    }
}
