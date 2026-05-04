package com.edugo.kmp.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.edugo.kmp.design.tokens.ExtendedColorScheme
import com.edugo.kmp.design.tokens.LocalExtendedColorScheme

/**
 * Theme genérico parametrizable. Una app que NO es EduGo puede pasar su propio
 * colorScheme y typography. Este componente no fija ninguna marca.
 */
@Composable
fun DSTheme(
    colorScheme: ColorScheme = lightColorScheme(),
    typography: Typography = BaseTypography,
    extendedColors: ExtendedColorScheme = ExtendedColorScheme.light(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalExtendedColorScheme provides extendedColors) {
        MaterialTheme(colorScheme = colorScheme, typography = typography) {
            CompositionLocalProvider(
                LocalContentColor provides colorScheme.onBackground,
                content = content,
            )
        }
    }
}
