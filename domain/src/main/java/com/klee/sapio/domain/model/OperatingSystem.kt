package com.klee.sapio.domain.model

object OperatingSystemName {
    const val E_OS = "eos"
    const val IODE_OS = "iodeos"
    const val CALYX_OS = "calyxos"
    const val GRAPHENE_OS = "grapheneos"
    const val LINEAGE_OS = "lineageos"
    const val OTHER = "other"
}

data class OperatingSystem(
    val name: String,
    val version: String,
    val androidVersion: Int
)
