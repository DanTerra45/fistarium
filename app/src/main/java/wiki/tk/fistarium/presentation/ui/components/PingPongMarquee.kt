package wiki.tk.fistarium.presentation.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay

@Composable
fun PingPongMarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(text, scrollState.maxValue) {
        if (scrollState.maxValue > 0) {
            while (true) {
                delay(2000)
                // Calculate duration based on distance to keep speed constant
                // e.g., 50 pixels per second -> duration = distance * 20
                val duration = scrollState.maxValue * 15 
                
                scrollState.animateScrollTo(
                    scrollState.maxValue, 
                    animationSpec = tween(durationMillis = duration, easing = LinearEasing)
                )
                delay(2000)
                scrollState.animateScrollTo(
                    0, 
                    animationSpec = tween(durationMillis = duration, easing = LinearEasing)
                )
            }
        }
    }

    Text(
        text = text,
        modifier = modifier.horizontalScroll(scrollState, enabled = false),
        style = style,
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = overflow
    )
}
