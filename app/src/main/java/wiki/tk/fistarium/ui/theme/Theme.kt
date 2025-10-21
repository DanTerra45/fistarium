package wiki.tk.fistarium.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FistRed80,
    onPrimary = FistBlack,
    primaryContainer = FistRed40,
    onPrimaryContainer = FistWhite,
    secondary = FistGold80,
    onSecondary = FistBlack,
    secondaryContainer = FistGold40,
    onSecondaryContainer = FistWhite,
    tertiary = FistGrey80,
    onTertiary = FistBlack,
    background = FistBlack,
    onBackground = FistWhite,
    surface = Color(0xFF1E1E1E),
    onSurface = FistWhite,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = FistGrey80
)

private val LightColorScheme = lightColorScheme(
    primary = FistRed40,
    onPrimary = FistWhite,
    primaryContainer = FistRed80,
    onPrimaryContainer = FistBlack,
    secondary = FistGold40,
    onSecondary = FistBlack,
    secondaryContainer = FistGold80,
    onSecondaryContainer = FistBlack,
    tertiary = FistGrey40,
    onTertiary = FistWhite,
    background = FistWhite,
    onBackground = FistBlack,
    surface = Color(0xFFFAFAFA),
    onSurface = FistBlack,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = FistGrey40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun FistariumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}