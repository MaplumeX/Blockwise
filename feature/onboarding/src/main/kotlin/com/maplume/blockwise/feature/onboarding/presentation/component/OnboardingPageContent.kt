package com.maplume.blockwise.feature.onboarding.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.feature.onboarding.domain.model.OnboardingPage
import kotlinx.coroutines.delay

/**
 * Content component for a single onboarding page.
 *
 * @param page The onboarding page data.
 * @param isVisible Whether this page is currently visible.
 * @param modifier Modifier to apply to the component.
 */
@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(100)
            showContent = true
        } else {
            showContent = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Image with animation
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(500)) +
                    scaleIn(
                        initialScale = 0.8f,
                        animationSpec = androidx.compose.animation.core.tween(500)
                    ),
            exit = fadeOut() + scaleOut()
        ) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = page.title,
                modifier = Modifier.size(240.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Title with animation
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 500,
                    delayMillis = 200
                )
            ),
            exit = fadeOut()
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description with animation
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 500,
                    delayMillis = 400
                )
            ),
            exit = fadeOut()
        ) {
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
