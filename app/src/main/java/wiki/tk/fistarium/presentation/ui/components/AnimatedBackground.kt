package wiki.tk.fistarium.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedBackground(
    content: @Composable BoxScope.() -> Unit
) {
    // Determine if we are in dark mode based on the current theme's background luminance
    // instead of the system setting, to respect the app-specific theme override.
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")

    // Animate the offset of the red glow
    val xOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "xOffset"
    )

    val yOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yOffset"
    )

    // Animate the alpha of the glow
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A1A1A), // Dark Grey
                Color(0xFF000000)  // Black
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF5F5F5), // Light Grey
                Color(0xFFFFFFFF)  // White
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        // Animated Red Glow
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = xOffset.dp, y = yOffset.dp)
                .size(400.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFB71C1C).copy(alpha = if (isDark) alpha else alpha * 0.5f), Color.Transparent)
                    )
                )
        )
        
        // Another subtle glow at bottom left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-xOffset).dp, y = (-yOffset).dp)
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF303F9F).copy(alpha = if (isDark) alpha * 0.5f else alpha * 0.3f), Color.Transparent)
                    )
                )
        )
        content()
    }
}
