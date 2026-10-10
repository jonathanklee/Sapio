package com.klee.sapio.domain

import com.klee.sapio.domain.model.BrokenFeature
import com.klee.sapio.domain.model.CachedDeviceApp
import com.klee.sapio.domain.model.Environment
import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.domain.model.EvaluationHistory
import com.klee.sapio.domain.model.VerifiedBootState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class ModelsTest {

    @Test
    fun `every broken feature exposes a distinct wire key`() {
        val keys = BrokenFeature.entries.map { it.key }

        assertEquals(BrokenFeature.entries.size, keys.toSet().size)
        assertEquals("notifications", BrokenFeature.NOTIFICATIONS.key)
        assertEquals("in_app_purchase", BrokenFeature.IN_APP_PURCHASE.key)
        assertEquals("augmented_reality", BrokenFeature.AUGMENTED_REALITY.key)
    }

    @Test
    fun `broken features are resolved back from their key`() {
        val resolved = BrokenFeature.entries.associateBy { it.key }

        assertEquals(BrokenFeature.LOGIN, resolved["login"])
        assertEquals(BrokenFeature.MAPS, resolved["maps"])
        assertNull(resolved["unknown"])
    }

    @Test
    fun `attestation values map to their boot state`() {
        assertEquals(VerifiedBootState.GREEN, VerifiedBootState.fromAttestationValue(0))
        assertEquals(VerifiedBootState.YELLOW, VerifiedBootState.fromAttestationValue(1))
        assertEquals(VerifiedBootState.ORANGE, VerifiedBootState.fromAttestationValue(2))
        assertEquals(VerifiedBootState.RED, VerifiedBootState.fromAttestationValue(3))
    }

    @Test
    fun `an unexpected attestation value is unknown`() {
        assertEquals(VerifiedBootState.UNKNOWN, VerifiedBootState.fromAttestationValue(-1))
        assertEquals(VerifiedBootState.UNKNOWN, VerifiedBootState.fromAttestationValue(99))
    }

    @Test
    fun `an evaluation history orders its evaluations by date`() {
        val older = evaluation("older", Date(1_000L))
        val newer = evaluation("newer", Date(2_000L))

        val history = EvaluationHistory.of(listOf(newer, older))

        assertEquals(listOf("older", "newer"), history?.evaluations?.map { it.name })
        assertEquals("newer", history?.current?.name)
    }

    @Test
    fun `an empty evaluation history is not built`() {
        assertNull(EvaluationHistory.of(emptyList()))
    }

    @Test
    fun `a single evaluation carries no trend`() {
        val history = EvaluationHistory.of(listOf(evaluation("only", Date(1_000L))))

        assertFalse(history!!.hasTrend)
    }

    @Test
    fun `several evaluations carry a trend`() {
        val history = EvaluationHistory.of(
            listOf(evaluation("older", Date(1_000L)), evaluation("newer", Date(2_000L)))
        )

        assertTrue(history!!.hasTrend)
    }

    @Test
    fun `an environment is identified by its gms and user type`() {
        assertEquals(Environment(1, 3), Environment(1, 3))
        assertFalse(Environment(1, 3) == Environment(1, 4))
    }

    @Test
    fun `a cached device app keeps its rating and timestamp`() {
        val cached = CachedDeviceApp("pkg", 2, 1_500L)

        assertEquals("pkg", cached.packageName)
        assertEquals(2, cached.rating)
        assertEquals(1_500L, cached.cachedAt)
        assertEquals(cached, cached.copy())
    }

    private fun evaluation(name: String, updatedAt: Date) = Evaluation(
        name = name,
        packageName = "pkg",
        iconUrl = null,
        rating = 1,
        microg = 1,
        secure = 3,
        updatedAt = updatedAt,
        createdAt = null,
        publishedAt = null,
        versionName = null,
        brokenFeatures = null
    )
}
