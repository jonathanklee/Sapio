package com.klee.sapio.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klee.sapio.model.Application
import com.klee.sapio.model.ApplicationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var applicationRepository: ApplicationsRepository

    var applications = MutableLiveData<List<Application>>()

    fun listApplications() {
        viewModelScope.launch {
            val result = applicationRepository.getFeedApplications()
            applications.postValue(result)
        }
    }
}
