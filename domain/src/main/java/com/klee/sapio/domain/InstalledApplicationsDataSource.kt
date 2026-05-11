package com.klee.sapio.domain

import com.klee.sapio.domain.model.InstalledApplication

interface InstalledApplicationsDataSource {
    fun listInstalledApplications(): List<InstalledApplication>

    fun getInstalledApplication(packageName: String): InstalledApplication?
}
