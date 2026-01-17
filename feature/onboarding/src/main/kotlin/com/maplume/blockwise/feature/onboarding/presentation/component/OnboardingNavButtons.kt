package com.maplume.blockwise.feature.onboarding.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Navigation buttons for the onboarding screen.
 *
 * @param isFirstPage Whether this is the first page.
 * @param isLastPage Whether this is the last page.
 * @param onBackClick Callback when back button is clicked.
 * @param onNextClick Callback when next/finish button is clicked.
 * @param onSkipClick Callback when skip button is clicked.
 * @param modifier Modifier to apply to the component.
 */
@Composable
fun OnboardingNavButtons(
    isFirstPage: Boolean,
    isLastPage: Boolean,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Skip or Back button
        AnimatedVisibility(
            visible = !isLastPage,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (isFirstPage) {
                TextButton(onClick = onSkipClick) {
                    Text(
                        text = "跳过",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            } else {
                TextButton(onClick = onBackClick) {
                    Text(
                        text = "上一步",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Placeholder to maintain layout when skip button is invisible
        if (isLastPage) {
            TextButton(
                onClick = onBackClick,
                enabled = !isFirstPage
            ) {
                Text(
                    text = "上一步",
                    color = if (isFirstPage) Color.Transparent else Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Next or Finish button
        Button(
            onClick = onNextClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (isLastPage) "开始使用" else "下一步"
            )
        }
    }
}
