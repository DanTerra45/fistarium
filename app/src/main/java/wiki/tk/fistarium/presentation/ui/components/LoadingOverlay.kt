package wiki.tk.fistarium.presentation.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * A reusable loading overlay component that shows a blur effect over the content
 * with a centered loading indicator.
 *
 * @param isLoading Whether to show the loading overlay
 * @param modifier Modifier for the container
 * @param loadingText Optional text to show below the loading indicator
 * @param blurRadius Blur radius for the background effect (only on Android 12+)
 * @param overlayAlpha Alpha value for the semi-transparent overlay
 * @param content The content to display behind the overlay
 */
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    loadingText: String? = null,
    blurRadius: Float = 10f,
    overlayAlpha: Float = 0.7f,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Main content with optional blur when loading
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isLoading && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.graphicsLayer {
                            renderEffect = RenderEffect
                                .createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
                                .asComposeRenderEffect()
                        }
                    } else if (isLoading) {
                        // Fallback for older devices: just dim the content
                        Modifier.alpha(0.3f)
                    } else {
                        Modifier
                    }
                )
        ) {
            content()
        }

        // Loading overlay
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            // On Android 12+, we have blur so use less opacity
                            Color.Black.copy(alpha = overlayAlpha * 0.5f)
                        } else {
                            // On older devices, use more opacity since there's no blur
                            Color.Black.copy(alpha = overlayAlpha)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    
                    if (loadingText != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = loadingText,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * A simpler full-screen loading indicator without content behind it.
 * Useful for initial app loading states.
 */
@Composable
fun FullScreenLoading(
    modifier: Modifier = Modifier,
    loadingText: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            
            if (loadingText != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = loadingText,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
