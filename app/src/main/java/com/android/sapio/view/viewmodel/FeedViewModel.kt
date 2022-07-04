package com.android.sapio.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sapio.model.Application
import com.android.sapio.model.ApplicationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(): ViewModel() {

    @Inject
    lateinit var applicationRepository: ApplicationsRepository

    var applications = MutableLiveData<List<Application>>()

    fun listApplications() {
        viewModelScope.launch {
            applicationRepository.refreshApplications()
            applications.postValue(applicationRepository.applications)
        }
    }
}
