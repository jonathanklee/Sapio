package com.android.sapio.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sapio.model.Application
import com.android.sapio.model.ApplicationsRepository
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val applicationRepository = ApplicationsRepository()

    val foundApplications = MutableLiveData<List<Application>>()

    fun searchApplication(pattern: String) {
        viewModelScope.launch {
            applicationRepository.searchApplications(pattern)
            foundApplications.postValue(applicationRepository.foundApplications)
        }
    }
}
