package com.android.sapio.view.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel : ViewModel() {

    var data = MutableLiveData<List<ParseObject>>()

    fun searchApp(pattern: String) {
        viewModelScope.launch {
            queryDatabase(pattern)
        }
    }

    private suspend fun queryDatabase(pattern: String) {
        withContext(Dispatchers.IO) {
            val query = ParseQuery.getQuery<ParseObject>("LibreApps")
            query.whereMatches("name", pattern, "i")
            data.postValue(query.find())
        }
    }
}
