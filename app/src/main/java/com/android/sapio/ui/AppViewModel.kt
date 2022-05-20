package com.android.sapio.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel : ViewModel() {

    var data = MutableLiveData<List<ParseObject>>()

    fun loadApps() {
        viewModelScope.launch {
            queryDatabase()
        }
    }

    private suspend fun queryDatabase() {
        withContext(Dispatchers.IO) {
            val query = ParseQuery.getQuery<ParseObject>("LibreApps")
            query.orderByDescending("updatedAt")
            data.postValue(query.find())
        }
    }
}
