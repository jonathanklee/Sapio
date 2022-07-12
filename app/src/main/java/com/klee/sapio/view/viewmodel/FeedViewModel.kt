package com.klee.sapio.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.model.RemoteApplication
import com.klee.sapio.model.RemoteApplicationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var applicationRepository: RemoteApplicationsRepository

    var applications = MutableLiveData<List<RemoteApplication>>()

    fun listApplications() {
        viewModelScope.launch {
            val result = applicationRepository.getFeedApplications()
            applications.postValue(result)
        }
    }
}
