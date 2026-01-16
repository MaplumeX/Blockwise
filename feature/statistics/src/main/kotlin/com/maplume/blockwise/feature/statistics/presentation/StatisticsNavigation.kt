package com.maplume.blockwise.feature.statistics.presentation

/**
 * Navigation destinations for the Statistics feature.
 */
object StatisticsNavigation {
    const val ROUTE = "statistics"

    // Detail routes
    const val ACTIVITY_DETAIL_ROUTE = "statistics/activity/{activityId}/{activityName}/{activityColor}"
    const val TAG_DETAIL_ROUTE = "statistics/tag/{tagId}/{tagName}/{tagColor}"

    /**
     * Create route for activity type detail screen.
     */
    fun activityDetailRoute(activityId: Long, activityName: String, activityColor: String): String {
        val encodedName = java.net.URLEncoder.encode(activityName, "UTF-8")
        val encodedColor = java.net.URLEncoder.encode(activityColor, "UTF-8")
        return "statistics/activity/$activityId/$encodedName/$encodedColor"
    }

    /**
     * Create route for tag detail screen.
     */
    fun tagDetailRoute(tagId: Long, tagName: String, tagColor: String): String {
        val encodedName = java.net.URLEncoder.encode(tagName, "UTF-8")
        val encodedColor = java.net.URLEncoder.encode(tagColor, "UTF-8")
        return "statistics/tag/$tagId/$encodedName/$encodedColor"
    }
}
