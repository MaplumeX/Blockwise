package com.maplume.blockwise.feature.timeentry.presentation.timeline

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("TimeEntryDraft duration")
class TimeEntryDraftDurationTest {

    @Test
    fun `durationSeconds handles same-minute second differences`() {
        val draft = TimeEntryDraft(
            entryId = 1L,
            startDate = LocalDate(2026, 1, 1),
            endDate = LocalDate(2026, 1, 1),
            startTime = LocalTime(10, 0, 5),
            endTime = LocalTime(10, 0, 8),
            activityId = 1L,
            tagIds = emptySet(),
            note = "",
            adjacentUpEntryId = null,
            adjacentDownEntryId = null
        )

        assertEquals(3, draft.durationSeconds)
    }

    @Test
    fun `durationSeconds handles cross-midnight second differences`() {
        val draft = TimeEntryDraft(
            entryId = 1L,
            startDate = LocalDate(2026, 1, 1),
            endDate = LocalDate(2026, 1, 2),
            startTime = LocalTime(23, 59, 58),
            endTime = LocalTime(0, 0, 2),
            activityId = 1L,
            tagIds = emptySet(),
            note = "",
            adjacentUpEntryId = null,
            adjacentDownEntryId = null
        )

        assertEquals(4, draft.durationSeconds)
    }
}
