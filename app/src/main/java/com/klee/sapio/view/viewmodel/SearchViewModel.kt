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
class SearchViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var applicationRepository: RemoteApplicationsRepository

    val foundApplications = MutableLiveData<List<RemoteApplication>>()

    fun searchApplication(pattern: String) {
        viewModelScope.launch {
            val result = applicationRepository.searchApplicationsFromStrapi(pattern)
            foundApplications.postValue(result)
        }
    }
}
