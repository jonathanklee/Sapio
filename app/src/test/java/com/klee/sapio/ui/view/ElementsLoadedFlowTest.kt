package com.klee.sapio.ui.view

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ElementsLoadedFlowTest {

    @Test
    fun `once emits only once when root disabled`() = runTest {
        val microgUser = MutableSharedFlow<Boolean>()
        val microgRoot = MutableSharedFlow<Boolean>()
        val bareAospUser = MutableSharedFlow<Boolean>()
        val bareAospRoot = MutableSharedFlow<Boolean>()
        val icon = MutableSharedFlow<Boolean>()

        val emissions = mutableListOf<Unit>()
        val job = launch {
            elementsLoadedFlow(
                rootEnabled = false,
                signals = ElementsLoadedSignals(
                    microgUserReceived = microgUser,
                    microgRootReceived = microgRoot,
                    bareAospUserReceived = bareAospUser,
                    bareAospRootReceived = bareAospRoot,
                    iconReceived = icon
                ),
                once = true
            ).collect { emissions.add(Unit) }
        }

        microgUser.emit(true)
        bareAospUser.emit(true)
        icon.emit(true)
        advanceUntilIdle()

        microgUser.emit(true)
        bareAospUser.emit(true)
        icon.emit(true)
        advanceUntilIdle()

        assertEquals(1, emissions.size)
        job.cancel()
    }

    @Test
    fun `once emits only once when root enabled`() = runTest {
        val microgUser = MutableSharedFlow<Boolean>()
        val microgRoot = MutableSharedFlow<Boolean>()
        val bareAospUser = MutableSharedFlow<Boolean>()
        val bareAospRoot = MutableSharedFlow<Boolean>()
        val icon = MutableSharedFlow<Boolean>()

        val emissions = mutableListOf<Unit>()
        val job = launch {
            elementsLoadedFlow(
                rootEnabled = true,
                signals = ElementsLoadedSignals(
                    microgUserReceived = microgUser,
                    microgRootReceived = microgRoot,
                    bareAospUserReceived = bareAospUser,
                    bareAospRootReceived = bareAospRoot,
                    iconReceived = icon
                ),
                once = true
            ).collect { emissions.add(Unit) }
        }

        microgUser.emit(true)
        microgRoot.emit(true)
        bareAospUser.emit(true)
        bareAospRoot.emit(true)
        icon.emit(true)
        advanceUntilIdle()

        microgUser.emit(true)
        microgRoot.emit(true)
        bareAospUser.emit(true)
        bareAospRoot.emit(true)
        icon.emit(true)
        advanceUntilIdle()

        assertEquals(1, emissions.size)
        job.cancel()
    }
}
