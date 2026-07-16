package com.klee.sapio.domain.model

enum class VerifiedBootState {
    GREEN,
    YELLOW,
    ORANGE,
    RED,
    UNKNOWN;

    companion object {
        private const val ATTESTATION_GREEN = 0
        private const val ATTESTATION_YELLOW = 1
        private const val ATTESTATION_ORANGE = 2
        private const val ATTESTATION_RED = 3

        fun fromAttestationValue(value: Int): VerifiedBootState {
            return when (value) {
                ATTESTATION_GREEN -> GREEN
                ATTESTATION_YELLOW -> YELLOW
                ATTESTATION_ORANGE -> ORANGE
                ATTESTATION_RED -> RED
                else -> UNKNOWN
            }
        }
    }
}
