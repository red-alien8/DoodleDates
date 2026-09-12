package com.spycodedoodledates.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spycodedoodledates.data.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DiaryRepository
) : ViewModel() {

    private val _currentYear = MutableStateFlow(2026)
    val currentYear: StateFlow<Int> = _currentYear

    private val _monthDataStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val monthDataStatus: StateFlow<Map<String, Boolean>> = _monthDataStatus

    init {
        refreshDataStatus()
    }

    fun setYear(year: Int) {
        _currentYear.value = year
        refreshDataStatus()
    }

    fun refreshDataStatus() {
        viewModelScope.launch {
            val statusMap = mutableMapOf<String, Boolean>()
            val year = _currentYear.value
            for (month in 1..12) {
                val monthKey = "$year-${month.toString().padStart(2, '0')}"
                statusMap[monthKey] = repository.hasDataForMonth(monthKey)
            }
            _monthDataStatus.value = statusMap
        }
    }
}
