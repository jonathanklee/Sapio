package com.klee.sapio

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import com.klee.sapio.domain.EvaluateAppUseCase
import com.klee.sapio.domain.EvaluationRepository
import com.klee.sapio.domain.model.Icon
import com.klee.sapio.domain.model.InstalledApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NONE

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = NONE, sdk = [Build.VERSION_CODES.M])
class EvaluateAppUseCaseTest {

    private lateinit var evaluateAppUseCase: EvaluateAppUseCase

    @Mock
    private lateinit var mockedEvaluationRepository: EvaluationRepository

    private lateinit var realInstalledApplication: InstalledApplication

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Create a real DeviceConfiguration instance since it's a final class
        val roboContext = org.robolectric.RuntimeEnvironment.getApplication()
        val deviceConfiguration = com.klee.sapio.data.DeviceConfiguration(roboContext)
        
        evaluateAppUseCase = EvaluateAppUseCase(mockedEvaluationRepository, deviceConfiguration)
        
        // Create a real InstalledApplication instance since it's a final class
        val fakeDrawable = ColorDrawable(Color.RED)
        realInstalledApplication = InstalledApplication(
            name = "Test App",
            packageName = "com.test.app",
            icon = fakeDrawable
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test_evaluateApp_withFailedIconUpload() = runTest {
        // Setup mock data for failed upload
        Mockito.`when`(mockedEvaluationRepository.uploadIcon(realInstalledApplication))
            .thenReturn(null)
        
        // Mock the existingIcon call to return empty list to avoid NPE
        Mockito.`when`(mockedEvaluationRepository.existingIcon("com.test.app.png"))
            .thenReturn(emptyList())

        var successCalled = false
        var errorCalled = false

        evaluateAppUseCase.invoke(realInstalledApplication, 1,
            onSuccess = { successCalled = true },
            onError = { errorCalled = true }
        )

        Assert.assertFalse("Success callback should not be called", successCalled)
        Assert.assertTrue("Error callback should be called", errorCalled)
    }

    @Test
    fun test_evaluateApp_withSuccessfulIconUpload() = runTest {
        // Setup mock data
        val fakeIcon = Icon(
            id = 123,
            name = "test.png",
            url = "http://example.com/test.png"
        )

        val fakeResponse = listOf(fakeIcon)
        val fakeExistingIcons = emptyList<Icon>()

        Mockito.`when`(mockedEvaluationRepository.uploadIcon(realInstalledApplication))
            .thenReturn(fakeResponse)
        Mockito.`when`(mockedEvaluationRepository.existingIcon("com.test.app.png"))
            .thenReturn(fakeExistingIcons)

        var successCalled = false
        var errorCalled = false

        evaluateAppUseCase.invoke(realInstalledApplication, 1,
            onSuccess = { successCalled = true },
            onError = { errorCalled = true }
        )

        Assert.assertTrue("Success callback should be called", successCalled)
        Assert.assertFalse("Error callback should not be called", errorCalled)
    }
}
