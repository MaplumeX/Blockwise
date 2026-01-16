package com.maplume.blockwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maplume.blockwise.core.designsystem.component.BlockwiseBottomNavigation
import com.maplume.blockwise.core.designsystem.component.BlockwiseNavigationItem
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import dagger.hilt.android.AndroidEntryPoint

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BlockwiseNavigationItem.TODAY.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BlockwiseNavigationItem.TODAY.route) {
                PlaceholderScreen("今日")
            }
            composable(BlockwiseNavigationItem.TIMELINE.route) {
                PlaceholderScreen("时间线")
            }
            composable(BlockwiseNavigationItem.STATISTICS.route) {
                PlaceholderScreen("统计")
            }
            composable(BlockwiseNavigationItem.GOALS.route) {
                PlaceholderScreen("目标")
            }
            composable(BlockwiseNavigationItem.SETTINGS.route) {
                PlaceholderScreen("设置")
            }
        }
    }
}

/**
 * Placeholder screen for navigation tabs
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
