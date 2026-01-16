package com.maplume.blockwise.feature.statistics.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Represents different time periods for statistics aggregation.
 */
sealed class StatisticsPeriod {
    abstract val startTime: Instant
    abstract val endTime: Instant
    abstract val label: String

    /**
     * Returns the previous period of the same type.
     */
    abstract fun previous(): StatisticsPeriod

    /**
     * Returns the next period of the same type.
     */
    abstract fun next(): StatisticsPeriod

    /**
     * Checks if this period contains the current time.
     */
    fun isCurrent(): Boolean {
        val now = Clock.System.now()
        return now in startTime..endTime
    }

    /**
     * Single day statistics.
     */
    data class Day(val date: LocalDate) : StatisticsPeriod() {
        private val timeZone = TimeZone.currentSystemDefault()

        override val startTime: Instant
            get() = date.atStartOfDayIn(timeZone)

        override val endTime: Instant
            get() = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

        override val label: String
            get() = "${date.monthNumber}月${date.dayOfMonth}日"

        override fun previous(): Day = Day(date.minus(1, DateTimeUnit.DAY))

        override fun next(): Day = Day(date.plus(1, DateTimeUnit.DAY))

        companion object {
            fun today(): Day {
                val now = Clock.System.now()
                val timeZone = TimeZone.currentSystemDefault()
                return Day(now.toLocalDateTime(timeZone).date)
            }
        }
    }

    /**
     * Week statistics (Monday to Sunday).
     */
    data class Week(val startDate: LocalDate) : StatisticsPeriod() {
        private val timeZone = TimeZone.currentSystemDefault()

        override val startTime: Instant
            get() = startDate.atStartOfDayIn(timeZone)

        override val endTime: Instant
            get() = startDate.plus(7, DateTimeUnit.DAY).atStartOfDayIn(timeZone)

        override val label: String
            get() {
                val endDate = startDate.plus(6, DateTimeUnit.DAY)
                return "${startDate.monthNumber}/${startDate.dayOfMonth} - ${endDate.monthNumber}/${endDate.dayOfMonth}"
            }

        override fun previous(): Week = Week(startDate.minus(7, DateTimeUnit.DAY))

        override fun next(): Week = Week(startDate.plus(7, DateTimeUnit.DAY))

        companion object {
            fun current(): Week {
                val now = Clock.System.now()
                val timeZone = TimeZone.currentSystemDefault()
                val today = now.toLocalDateTime(timeZone).date
                // Find Monday of current week (dayOfWeek: 1=Monday, 7=Sunday)
                val daysFromMonday = today.dayOfWeek.ordinal
                val monday = today.minus(daysFromMonday, DateTimeUnit.DAY)
                return Week(monday)
            }
        }
    }

    /**
     * Month statistics.
     */
    data class Month(val year: Int, val month: Int) : StatisticsPeriod() {
        private val timeZone = TimeZone.currentSystemDefault()

        override val startTime: Instant
            get() = LocalDate(year, month, 1).atStartOfDayIn(timeZone)

        override val endTime: Instant
            get() {
                val nextMonth = if (month == 12) {
                    LocalDate(year + 1, 1, 1)
                } else {
                    LocalDate(year, month + 1, 1)
                }
                return nextMonth.atStartOfDayIn(timeZone)
            }

        override val label: String
            get() = "${year}年${month}月"

        override fun previous(): Month {
            return if (month == 1) {
                Month(year - 1, 12)
            } else {
                Month(year, month - 1)
            }
        }

        override fun next(): Month {
            return if (month == 12) {
                Month(year + 1, 1)
            } else {
                Month(year, month + 1)
            }
        }

        companion object {
            fun current(): Month {
                val now = Clock.System.now()
                val timeZone = TimeZone.currentSystemDefault()
                val today = now.toLocalDateTime(timeZone).date
                return Month(today.year, today.monthNumber)
            }
        }
    }

    /**
     * Year statistics.
     */
    data class Year(val year: Int) : StatisticsPeriod() {
        private val timeZone = TimeZone.currentSystemDefault()

        override val startTime: Instant
            get() = LocalDate(year, 1, 1).atStartOfDayIn(timeZone)

        override val endTime: Instant
            get() = LocalDate(year + 1, 1, 1).atStartOfDayIn(timeZone)

        override val label: String
            get() = "${year}年"

        override fun previous(): Year = Year(year - 1)

        override fun next(): Year = Year(year + 1)

        companion object {
            fun current(): Year {
                val now = Clock.System.now()
                val timeZone = TimeZone.currentSystemDefault()
                return Year(now.toLocalDateTime(timeZone).date.year)
            }
        }
    }

    /**
     * Custom date range statistics.
     */
    data class Custom(
        override val startTime: Instant,
        override val endTime: Instant
    ) : StatisticsPeriod() {
        private val timeZone = TimeZone.currentSystemDefault()

        override val label: String
            get() {
                val start = startTime.toLocalDateTime(timeZone).date
                val end = endTime.toLocalDateTime(timeZone).date
                return "${start.monthNumber}/${start.dayOfMonth} - ${end.monthNumber}/${end.dayOfMonth}"
            }

        override fun previous(): Custom {
            val duration = endTime - startTime
            return Custom(startTime - duration, endTime - duration)
        }

        override fun next(): Custom {
            val duration = endTime - startTime
            return Custom(startTime + duration, endTime + duration)
        }
    }
}
