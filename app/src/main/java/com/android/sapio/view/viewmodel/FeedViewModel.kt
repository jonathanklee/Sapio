package com.android.sapio.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sapio.model.Application
import com.android.sapio.model.ApplicationsRepository
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val applicationRepository = ApplicationsRepository()

    var applications = MutableLiveData<List<Application>>()

    fun listApplications() {
        viewModelScope.launch {
            applicationRepository.refreshApplications()
            applications.postValue(applicationRepository.applications)
        }
    }
}
