package com.maplume.blockwise.feature.timeentry.presentation

/**
 * Navigation destinations for the TimeEntry feature.
 */
object TimeEntryNavigation {
    const val ROUTE = "time_entry"
    const val ADD_ENTRY_ROUTE = "time_entry/add"
    const val EDIT_ENTRY_ROUTE = "time_entry/edit/{entryId}"

    // Timer routes
    const val TIMER_ROUTE = "timer"

    // Timeline routes
    const val TIMELINE_ROUTE = "timeline"

    // Time block routes
    const val TIME_BLOCK_ROUTE = "time_block"
    const val TIME_BLOCK_CREATE_ROUTE = "time_block/create/{date}/{hour}/{minute}"

    // Activity type routes
    const val ACTIVITY_TYPE_LIST_ROUTE = "activity_types"
    const val ACTIVITY_TYPE_ADD_ROUTE = "activity_types/add"
    const val ACTIVITY_TYPE_EDIT_ROUTE = "activity_types/edit/{activityTypeId}"

    // Tag routes
    const val TAG_MANAGEMENT_ROUTE = "tags"

    fun editEntryRoute(entryId: Long) = "time_entry/edit/$entryId"
    fun editActivityTypeRoute(activityTypeId: Long) = "activity_types/edit/$activityTypeId"
    fun timeBlockCreateRoute(date: String, hour: Int, minute: Int) =
        "time_block/create/$date/$hour/$minute"
}
