package com.klee.sapio.domain

interface AppSettings {
    fun getUnsafeConfigurationLevel(): Int

    fun isUnsafeConfigurationEnabled(): Boolean
}
