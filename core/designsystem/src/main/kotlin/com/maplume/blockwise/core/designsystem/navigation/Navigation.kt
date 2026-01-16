package com.maplume.blockwise.core.designsystem.navigation

/**
 * Navigation route constants
 */
object BlockwiseRoutes {
    // Main tabs
    const val TODAY = "today"
    const val TIMELINE = "timeline"
    const val STATISTICS = "statistics"
    const val GOALS = "goals"
    const val SETTINGS = "settings"

    // Detail screens
    const val ACTIVITY_DETAIL = "activity/{activityId}"
    const val GOAL_DETAIL = "goal/{goalId}"
    const val TAG_MANAGEMENT = "tag_management"

    // Helper functions for routes with arguments
    fun activityDetail(activityId: Long) = "activity/$activityId"
    fun goalDetail(goalId: Long) = "goal/$goalId"
}

/**
 * Navigation argument keys
 */
object NavArgs {
    const val ACTIVITY_ID = "activityId"
    const val GOAL_ID = "goalId"
}
