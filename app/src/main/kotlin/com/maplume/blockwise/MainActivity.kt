package com.maplume.blockwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.maplume.blockwise.core.designsystem.component.BlockwiseBottomNavigation
import com.maplume.blockwise.core.designsystem.component.BlockwiseNavigationItem
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.feature.statistics.presentation.StatisticsNavigation
import com.maplume.blockwise.feature.statistics.presentation.StatisticsScreen
import com.maplume.blockwise.feature.statistics.presentation.component.ActivityTypeDetailScreen
import com.maplume.blockwise.feature.statistics.presentation.component.TagDetailScreen
import com.maplume.blockwise.feature.timeentry.presentation.TimeEntryNavigation
import com.maplume.blockwise.feature.timeentry.presentation.activitytype.ActivityTypeEditScreen
import com.maplume.blockwise.feature.timeentry.presentation.activitytype.ActivityTypeListScreen
import com.maplume.blockwise.feature.timeentry.presentation.tag.TagManagementScreen
import com.maplume.blockwise.feature.timeentry.presentation.timeblock.TimeBlockScreen
import com.maplume.blockwise.feature.timeentry.presentation.timeentry.TimeEntryEditScreen
import com.maplume.blockwise.feature.timeentry.presentation.timeline.TimelineScreen
import com.maplume.blockwise.feature.timeentry.presentation.timer.TimerScreen
import com.maplume.blockwise.feature.goal.presentation.GoalNavigation
import com.maplume.blockwise.feature.goal.presentation.detail.GoalDetailScreen
import com.maplume.blockwise.feature.goal.presentation.edit.GoalEditScreen
import com.maplume.blockwise.feature.goal.presentation.list.GoalListScreen
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Main activity of the Blockwise application.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlockwiseTheme {
                BlockwiseApp()
            }
        }
    }
}

@Composable
fun BlockwiseApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BlockwiseNavigationItem.TODAY.route

    // Determine if bottom bar should be shown
    val showBottomBar = currentRoute in listOf(
        BlockwiseNavigationItem.TODAY.route,
        BlockwiseNavigationItem.TIMELINE.route,
        BlockwiseNavigationItem.STATISTICS.route,
        BlockwiseNavigationItem.GOALS.route,
        BlockwiseNavigationItem.SETTINGS.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BlockwiseBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BlockwiseNavigationItem.TODAY.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ==================== Main Tabs ====================

            // Today - Timer + Time Block View
            composable(BlockwiseNavigationItem.TODAY.route) {
                TimerScreen(
                    onNavigateToActivityTypes = {
                        navController.navigate(TimeEntryNavigation.ACTIVITY_TYPE_LIST_ROUTE)
                    },
                    onNavigateToTimeBlock = {
                        navController.navigate(TimeEntryNavigation.TIME_BLOCK_ROUTE)
                    }
                )
            }

            // Timeline
            composable(BlockwiseNavigationItem.TIMELINE.route) {
                TimelineScreen(
                    onNavigateToEdit = { entryId ->
                        navController.navigate(TimeEntryNavigation.editEntryRoute(entryId))
                    }
                )
            }

            // Statistics
            composable(BlockwiseNavigationItem.STATISTICS.route) {
                StatisticsScreen(
                    onNavigateToActivityDetail = { activityId ->
                        // Navigation handled via StatisticsNavigation routes
                    },
                    onNavigateToTagDetail = { tagId ->
                        // Navigation handled via StatisticsNavigation routes
                    }
                )
            }

            // Statistics Activity Detail
            composable(
                route = StatisticsNavigation.ACTIVITY_DETAIL_ROUTE,
                arguments = listOf(
                    navArgument("activityId") { type = NavType.LongType },
                    navArgument("activityName") { type = NavType.StringType },
                    navArgument("activityColor") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getLong("activityId") ?: 0L
                val activityName = URLDecoder.decode(
                    backStackEntry.arguments?.getString("activityName") ?: "",
                    "UTF-8"
                )
                val activityColor = URLDecoder.decode(
                    backStackEntry.arguments?.getString("activityColor") ?: "#135BEC",
                    "UTF-8"
                )
                ActivityTypeDetailScreen(
                    activityId = activityId,
                    activityName = activityName,
                    activityColor = activityColor,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Statistics Tag Detail
            composable(
                route = StatisticsNavigation.TAG_DETAIL_ROUTE,
                arguments = listOf(
                    navArgument("tagId") { type = NavType.LongType },
                    navArgument("tagName") { type = NavType.StringType },
                    navArgument("tagColor") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tagId = backStackEntry.arguments?.getLong("tagId") ?: 0L
                val tagName = URLDecoder.decode(
                    backStackEntry.arguments?.getString("tagName") ?: "",
                    "UTF-8"
                )
                val tagColor = URLDecoder.decode(
                    backStackEntry.arguments?.getString("tagColor") ?: "#135BEC",
                    "UTF-8"
                )
                TagDetailScreen(
                    tagId = tagId,
                    tagName = tagName,
                    tagColor = tagColor,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Goals
            composable(BlockwiseNavigationItem.GOALS.route) {
                GoalListScreen(
                    onNavigateToAdd = {
                        navController.navigate(GoalNavigation.ADD_GOAL_ROUTE)
                    },
                    onNavigateToEdit = { goalId ->
                        navController.navigate(GoalNavigation.editGoalRoute(goalId))
                    },
                    onNavigateToDetail = { goalId ->
                        navController.navigate(GoalNavigation.detailGoalRoute(goalId))
                    }
                )
            }

            // Add Goal
            composable(GoalNavigation.ADD_GOAL_ROUTE) {
                GoalEditScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Edit Goal
            composable(
                route = GoalNavigation.EDIT_GOAL_ROUTE,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType })
            ) {
                GoalEditScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Goal Detail
            composable(
                route = GoalNavigation.DETAIL_GOAL_ROUTE,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType })
            ) {
                GoalDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { goalId ->
                        navController.navigate(GoalNavigation.editGoalRoute(goalId))
                    }
                )
            }

            // Settings
            composable(BlockwiseNavigationItem.SETTINGS.route) {
                SettingsScreen(
                    onNavigateToActivityTypes = {
                        navController.navigate(TimeEntryNavigation.ACTIVITY_TYPE_LIST_ROUTE)
                    },
                    onNavigateToTags = {
                        navController.navigate(TimeEntryNavigation.TAG_MANAGEMENT_ROUTE)
                    }
                )
            }

            // ==================== Time Entry Routes ====================

            // Time Block View
            composable(TimeEntryNavigation.TIME_BLOCK_ROUTE) {
                TimeBlockScreen(
                    onNavigateToEdit = { entryId ->
                        navController.navigate(TimeEntryNavigation.editEntryRoute(entryId))
                    },
                    onNavigateToCreate = { date, time ->
                        navController.navigate(
                            TimeEntryNavigation.timeBlockCreateRoute(
                                date.toString(),
                                time?.hour ?: 0,
                                time?.minute ?: 0
                            )
                        )
                    }
                )
            }

            // Add Time Entry
            composable(TimeEntryNavigation.ADD_ENTRY_ROUTE) {
                TimeEntryEditScreen(
                    entryId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Edit Time Entry
            composable(
                route = TimeEntryNavigation.EDIT_ENTRY_ROUTE,
                arguments = listOf(navArgument("entryId") { type = NavType.LongType })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getLong("entryId")
                TimeEntryEditScreen(
                    entryId = entryId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Create from Time Block (with pre-filled time)
            composable(
                route = TimeEntryNavigation.TIME_BLOCK_CREATE_ROUTE,
                arguments = listOf(
                    navArgument("date") { type = NavType.StringType },
                    navArgument("hour") { type = NavType.IntType },
                    navArgument("minute") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val dateStr = backStackEntry.arguments?.getString("date")
                val hour = backStackEntry.arguments?.getInt("hour") ?: 0
                val minute = backStackEntry.arguments?.getInt("minute") ?: 0

                TimeEntryEditScreen(
                    entryId = null,
                    prefilledDate = dateStr?.let { LocalDate.parse(it) },
                    prefilledTime = LocalTime(hour, minute),
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ==================== Activity Type Routes ====================

            // Activity Type List
            composable(TimeEntryNavigation.ACTIVITY_TYPE_LIST_ROUTE) {
                ActivityTypeListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { activityTypeId ->
                        if (activityTypeId != null) {
                            navController.navigate(
                                TimeEntryNavigation.editActivityTypeRoute(activityTypeId)
                            )
                        } else {
                            navController.navigate(TimeEntryNavigation.ACTIVITY_TYPE_ADD_ROUTE)
                        }
                    }
                )
            }

            // Add Activity Type
            composable(TimeEntryNavigation.ACTIVITY_TYPE_ADD_ROUTE) {
                ActivityTypeEditScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Edit Activity Type
            composable(
                route = TimeEntryNavigation.ACTIVITY_TYPE_EDIT_ROUTE,
                arguments = listOf(navArgument("activityTypeId") { type = NavType.LongType })
            ) {
                ActivityTypeEditScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ==================== Tag Routes ====================

            // Tag Management
            composable(TimeEntryNavigation.TAG_MANAGEMENT_ROUTE) {
                TagManagementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Settings screen with navigation to management screens.
 */
@Composable
fun SettingsScreen(
    onNavigateToActivityTypes: () -> Unit,
    onNavigateToTags: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SettingsItem(
            title = "活动类型管理",
            subtitle = "管理时间记录的活动分类",
            onClick = onNavigateToActivityTypes
        )

        SettingsItem(
            title = "标签管理",
            subtitle = "管理时间记录的标签",
            onClick = onNavigateToTags
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Placeholder screen for features under development.
 */
@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title)
    }
}
