package com.maplume.blockwise.feature.statistics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.feature.statistics.domain.model.StatisticsPeriod
import com.maplume.blockwise.feature.statistics.domain.usecase.GetPeriodStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the statistics screen.
 * Manages period selection and statistics data loading.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getPeriodStatistics: GetPeriodStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    /**
     * Handle UI events.
     */
    fun onEvent(event: StatisticsEvent) {
        when (event) {
            is StatisticsEvent.SelectPeriodType -> selectPeriodType(event.type)
            is StatisticsEvent.NavigateToPrevious -> navigateToPrevious()
            is StatisticsEvent.NavigateToNext -> navigateToNext()
            is StatisticsEvent.Refresh -> loadStatistics()
            is StatisticsEvent.NavigateToActivityDetail -> { /* Handled by UI */ }
            is StatisticsEvent.NavigateToTagDetail -> { /* Handled by UI */ }
        }
    }

    /**
     * Select a new period type (Day, Week, Month, Year).
     */
    private fun selectPeriodType(type: PeriodType) {
        if (type == _uiState.value.periodType) return

        val newPeriod = type.createCurrentPeriod()
        _uiState.update {
            it.copy(
                periodType = type,
                currentPeriod = newPeriod,
                isLoading = true,
                error = null
            )
        }
        loadStatistics()
    }

    /**
     * Navigate to the previous period.
     */
    private fun navigateToPrevious() {
        val previousPeriod = _uiState.value.currentPeriod.previous()
        _uiState.update {
            it.copy(
                currentPeriod = previousPeriod,
                isLoading = true,
                error = null
            )
        }
        loadStatistics()
    }

    /**
     * Navigate to the next period.
     */
    private fun navigateToNext() {
        if (_uiState.value.isCurrentPeriod) return

        val nextPeriod = _uiState.value.currentPeriod.next()
        _uiState.update {
            it.copy(
                currentPeriod = nextPeriod,
                isLoading = true,
                error = null
            )
        }
        loadStatistics()
    }

    /**
     * Load statistics for the current period.
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getPeriodStatistics(_uiState.value.currentPeriod)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "加载统计数据失败"
                        )
                    }
                }
                .collect { statistics ->
                    _uiState.update {
                        it.copy(
                            statistics = statistics,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
}
