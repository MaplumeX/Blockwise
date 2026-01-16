package com.maplume.blockwise.feature.statistics.presentation.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.core.domain.model.DailyTrend
import com.maplume.blockwise.core.domain.repository.StatisticsRepository
import com.maplume.blockwise.feature.statistics.domain.model.StatisticsPeriod
import com.maplume.blockwise.feature.statistics.domain.usecase.GetDailyTrendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for tag detail screen.
 */
data class TagDetailUiState(
    val tagId: Long = 0,
    val totalMinutes: Int = 0,
    val dailyTrends: List<DailyTrend> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for tag detail screen.
 */
@HiltViewModel
class TagDetailViewModel @Inject constructor(
    private val repository: StatisticsRepository,
    private val getDailyTrends: GetDailyTrendsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagDetailUiState())
    val uiState: StateFlow<TagDetailUiState> = _uiState.asStateFlow()

    /**
     * Load statistics for a specific tag.
     */
    fun loadTagStats(tagId: Long) {
        if (tagId == _uiState.value.tagId && _uiState.value.totalMinutes > 0) {
            return
        }

        _uiState.update { it.copy(tagId = tagId, isLoading = true, error = null) }

        val period = StatisticsPeriod.Month.current()

        viewModelScope.launch {
            try {
                val totalMinutes = repository.getTotalMinutesForTag(
                    tagId,
                    period.startTime,
                    period.endTime
                )
                _uiState.update { it.copy(totalMinutes = totalMinutes) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }

        viewModelScope.launch {
            getDailyTrends.forTag(tagId, period.startTime, period.endTime)
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
