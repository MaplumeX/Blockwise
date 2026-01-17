package com.maplume.blockwise.feature.settings.domain.model

/**
 * Aggregated application settings.
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val autoBackupEnabled: Boolean = false,
    val lastBackupTime: Long? = null
)

/**
 * Notification-related settings.
 */
data class NotificationSettings(
    /** Daily reminder notification */
    val dailyReminderEnabled: Boolean = false,
    /** Daily reminder time in minutes from midnight (e.g., 480 = 08:00) */
    val dailyReminderTime: Int = 480,
    /** Goal progress notification */
    val goalProgressEnabled: Boolean = true,
    /** Timer running notification */
    val timerNotificationEnabled: Boolean = true
)
