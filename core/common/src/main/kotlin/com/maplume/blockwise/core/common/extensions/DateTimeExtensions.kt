package com.maplume.blockwise.core.common.extensions

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Get the current instant.
 */
fun Clock.System.nowInstant(): Instant = now()

/**
 * Get the current local date in the system's default timezone.
 */
fun Clock.System.nowLocalDate(): LocalDate =
    now().toLocalDateTime(TimeZone.currentSystemDefault()).date

/**
 * Get the current local date-time in the system's default timezone.
 */
fun Clock.System.nowLocalDateTime(): LocalDateTime =
    now().toLocalDateTime(TimeZone.currentSystemDefault())

/**
 * Convert Instant to LocalDateTime in system timezone.
 */
fun Instant.toSystemLocalDateTime(): LocalDateTime =
    toLocalDateTime(TimeZone.currentSystemDefault())

/**
 * Convert Instant to LocalDate in system timezone.
 */
fun Instant.toSystemLocalDate(): LocalDate =
    toLocalDateTime(TimeZone.currentSystemDefault()).date
