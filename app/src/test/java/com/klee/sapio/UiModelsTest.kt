package com.klee.sapio

import android.graphics.Bitmap
import android.os.Build
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.ui.model.InstalledAppWithRating
import com.klee.sapio.ui.model.SharedEvaluation
import com.klee.sapio.ui.state.ChooseAppUiState
import com.klee.sapio.ui.state.MyAppsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class UiModelsTest {

    private val icon: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    @Test
    fun `a shared evaluation carries a rating per environment`() {
        val shared = SharedEvaluation("Sapio", "com.klee.sapio", icon, 1, 2)

        assertEquals("Sapio", shared.name)
        assertEquals("com.klee.sapio", shared.packageName)
        assertEquals(icon, shared.icon)
        assertEquals(1, shared.ratingMicrog)
        assertEquals(2, shared.ratingBareAOSP)
    }

    @Test
    fun `a shared evaluation has no broken feature by default`() {
        val shared = SharedEvaluation("Sapio", "com.klee.sapio", icon, 1, 1)

        assertNull(shared.brokenFeaturesMicrog)
        assertNull(shared.brokenFeaturesBareAOSP)
    }

    @Test
    fun `a shared evaluation keeps the broken features of each environment`() {
        val shared = SharedEvaluation(
            name = "Sapio",
            packageName = "com.klee.sapio",
            icon = icon,
            ratingMicrog = 2,
            ratingBareAOSP = 3,
            brokenFeaturesMicrog = listOf("in_app_purchase"),
            brokenFeaturesBareAOSP = listOf("login", "maps")
        )

        assertEquals(listOf("in_app_purchase"), shared.brokenFeaturesMicrog)
        assertEquals(listOf("login", "maps"), shared.brokenFeaturesBareAOSP)
        assertEquals(shared, shared.copy())
    }

    @Test
    fun `choosing an application starts from an empty loading state`() {
        val state = ChooseAppUiState()

        assertTrue(state.apps.isEmpty())
        assertTrue(state.isLoading)
    }

    @Test
    fun `a loaded application list clears the loading flag`() {
        val app = InstalledApplication("Sapio", "com.klee.sapio", "2.9.1")
        val state = ChooseAppUiState().copy(apps = listOf(app), isLoading = false)

        assertEquals(listOf(app), state.apps)
        assertFalse(state.isLoading)
    }

    @Test
    fun `device applications start from an idle state`() {
        val state = MyAppsUiState()

        assertTrue(state.items.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertEquals(0, state.progress)
    }

    @Test
    fun `a device application without an evaluation carries no rating`() {
        val app = InstalledApplication("Sapio", "com.klee.sapio", "2.9.1")
        val item = InstalledAppWithRating(app, null)

        assertEquals(app, item.installedApp)
        assertNull(item.evaluation)
    }
}
