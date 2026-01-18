package com.klee.sapio.ui.view

import kotlinx.coroutines.flow.Flow

internal data class ElementsLoadedSignals(
    val microgUserReceived: Flow<Boolean>,
    val microgRootReceived: Flow<Boolean>,
    val bareAospUserReceived: Flow<Boolean>,
    val bareAospRootReceived: Flow<Boolean>,
    val iconReceived: Flow<Boolean>
)
