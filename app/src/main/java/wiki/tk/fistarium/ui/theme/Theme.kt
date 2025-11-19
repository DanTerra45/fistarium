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
    primary = TekkenBlue,
    onPrimary = TekkenWhite,
    primaryContainer = TekkenPurple,
    onPrimaryContainer = TekkenWhite,
    secondary = TekkenElectricBlue,
    onSecondary = TekkenDarkBackground,
    secondaryContainer = TekkenGold,
    onSecondaryContainer = TekkenDarkBackground,
    tertiary = TekkenRed,
    onTertiary = TekkenWhite,
    background = TekkenDarkBackground,
    onBackground = TekkenWhite,
    surface = TekkenSurface,
    onSurface = TekkenWhite,
    surfaceVariant = TekkenSurfaceVariant,
    onSurfaceVariant = TekkenGrey
)

private val LightColorScheme = lightColorScheme(
    primary = TekkenBlue,
    onPrimary = TekkenWhite,
    primaryContainer = TekkenPurple,
    onPrimaryContainer = TekkenWhite,
    secondary = TekkenElectricBlue,
    onSecondary = TekkenDarkBackground,
    secondaryContainer = TekkenGold,
    onSecondaryContainer = TekkenDarkBackground,
    tertiary = TekkenRed,
    onTertiary = TekkenWhite,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF121212),
    surface = Color.White,
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF424242)
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