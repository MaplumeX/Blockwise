package com.maplume.blockwise.feature.goal.presentation

/**
 * Navigation destinations for the Goal feature.
 */
object GoalNavigation {
    const val ROUTE = "goal"
    const val ADD_GOAL_ROUTE = "goal/add"
    const val EDIT_GOAL_ROUTE = "goal/edit/{goalId}"

    fun editGoalRoute(goalId: Long) = "goal/edit/$goalId"
}
