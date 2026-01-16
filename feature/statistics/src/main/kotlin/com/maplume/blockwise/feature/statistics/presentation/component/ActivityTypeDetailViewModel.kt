package com.maplume.blockwise.feature.statistics.presentation.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.core.domain.model.DailyTrend
import com.maplume.blockwise.core.domain.model.StatisticsSummary
import com.maplume.blockwise.feature.statistics.domain.model.StatisticsPeriod
import com.maplume.blockwise.feature.statistics.domain.usecase.GetDailyTrendsUseCase
import com.maplume.blockwise.feature.statistics.domain.usecase.GetStatisticsSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for activity type detail screen.
 */
data class ActivityTypeDetailUiState(
    val activityId: Long = 0,
    val summary: StatisticsSummary? = null,
    val dailyTrends: List<DailyTrend> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for activity type detail screen.
 */
@HiltViewModel
class ActivityTypeDetailViewModel @Inject constructor(
    private val getDailyTrends: GetDailyTrendsUseCase,
    private val getStatisticsSummary: GetStatisticsSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityTypeDetailUiState())
    val uiState: StateFlow<ActivityTypeDetailUiState> = _uiState.asStateFlow()

    /**
     * Load statistics for a specific activity type.
     */
    fun loadActivityStats(activityId: Long) {
        if (activityId == _uiState.value.activityId && _uiState.value.summary != null) {
            return
        }

        _uiState.update { it.copy(activityId = activityId, isLoading = true, error = null) }

        val period = StatisticsPeriod.Month.current()

        viewModelScope.launch {
            try {
                val summary = getStatisticsSummary(period.startTime, period.endTime)
                _uiState.update { it.copy(summary = summary) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }

        viewModelScope.launch {
            getDailyTrends(period.startTime, period.endTime)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { trends ->
                    _uiState.update {
                        it.copy(dailyTrends = trends, isLoading = false)
                    }
                }
        }
    }
}
