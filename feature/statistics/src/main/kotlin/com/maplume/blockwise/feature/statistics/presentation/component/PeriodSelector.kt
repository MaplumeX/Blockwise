package com.maplume.blockwise.feature.statistics.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.feature.statistics.presentation.PeriodType

/**
 * Period selector component with tabs and navigation arrows.
 *
 * @param selectedType Currently selected period type.
 * @param periodLabel Label for the current period.
 * @param isCurrentPeriod Whether the current period is the latest.
 * @param onTypeSelected Callback when a period type is selected.
 * @param onPreviousClick Callback when previous button is clicked.
 * @param onNextClick Callback when next button is clicked.
 * @param modifier Modifier for the component.
 */
@Composable
fun PeriodSelector(
    selectedType: PeriodType,
    periodLabel: String,
    isCurrentPeriod: Boolean,
    onTypeSelected: (PeriodType) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Period type tabs
            PeriodTypeTabs(
                selectedType = selectedType,
                onTypeSelected = onTypeSelected,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Period type tab row.
 */
@Composable
private fun PeriodTypeTabs(
    selectedType: PeriodType,
    onTypeSelected: (PeriodType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PeriodType.entries.forEach { type ->
            PeriodTypeTab(
                type = type,
                isSelected = type == selectedType,
                onClick = { onTypeSelected(type) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Single period type tab.
 */
@Composable
private fun PeriodTypeTab(
    type: PeriodType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "tab_background"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "tab_text"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = type.displayName,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Period navigation bar with arrows and label.
 *
 * @param periodLabel Label for the current period.
 * @param isCurrentPeriod Whether the current period is the latest.
 * @param onPreviousClick Callback when previous button is clicked.
 * @param onNextClick Callback when next button is clicked.
 * @param modifier Modifier for the component.
 */
@Composable
fun PeriodNavigationBar(
    periodLabel: String,
    isCurrentPeriod: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button
        IconButton(
            onClick = onPreviousClick,
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "上一个周期",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Period label
        Text(
            text = periodLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        // Next button
        IconButton(
            onClick = onNextClick,
            enabled = !isCurrentPeriod,
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isCurrentPeriod) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "下一个周期",
                tint = if (isCurrentPeriod) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
