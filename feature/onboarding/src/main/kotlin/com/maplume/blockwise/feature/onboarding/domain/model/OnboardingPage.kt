package com.maplume.blockwise.feature.onboarding.domain.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.maplume.blockwise.feature.onboarding.R

/**
 * Represents a single onboarding page with its content.
 */
data class OnboardingPage(
    val title: String,
    val description: String,
    @DrawableRes val imageRes: Int,
    val backgroundColor: Color
)

/**
 * Predefined onboarding content for the app.
 */
object OnboardingContent {
    val pages = listOf(
        OnboardingPage(
            title = "记录时间，掌控生活",
            description = "使用柳比歇夫时间管理法，精确记录每一分钟的去向，让时间不再悄悄溜走",
            imageRes = R.drawable.onboarding_time_tracking,
            backgroundColor = Color(0xFF1976D2)
        ),
        OnboardingPage(
            title = "可视化分析",
            description = "通过直观的图表和统计，了解你的时间分配，发现改进空间",
            imageRes = R.drawable.onboarding_statistics,
            backgroundColor = Color(0xFF388E3C)
        ),
        OnboardingPage(
            title = "设定目标，持续进步",
            description = "制定时间目标，跟踪完成进度，养成良好的时间管理习惯",
            imageRes = R.drawable.onboarding_goals,
            backgroundColor = Color(0xFFF57C00)
        )
    )
}
