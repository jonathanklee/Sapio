package com.klee.sapio.ui.view

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.take

internal fun elementsLoadedFlow(
    rootEnabled: Boolean,
    signals: ElementsLoadedSignals,
    once: Boolean
): Flow<Unit> {
    val flow = if (rootEnabled) {
        combine(
            signals.microgUserReceived,
            signals.microgRootReceived,
            signals.bareAospUserReceived,
            signals.bareAospRootReceived,
            signals.iconReceived
        ) { _, _, _, _, _ ->
            Unit
        }
    } else {
        combine(
            signals.microgUserReceived,
            signals.bareAospUserReceived,
            signals.iconReceived
        ) { _, _, _ ->
            Unit
        }
    }

    return if (once) flow.take(1) else flow
}
