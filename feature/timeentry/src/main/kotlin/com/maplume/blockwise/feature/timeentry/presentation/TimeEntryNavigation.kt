package com.maplume.blockwise.feature.timeentry.presentation

/**
 * Navigation destinations for the TimeEntry feature.
 */
object TimeEntryNavigation {
    const val ROUTE = "time_entry"
    const val ADD_ENTRY_ROUTE = "time_entry/add"
    const val EDIT_ENTRY_ROUTE = "time_entry/edit/{entryId}"

    fun editEntryRoute(entryId: Long) = "time_entry/edit/$entryId"
}
