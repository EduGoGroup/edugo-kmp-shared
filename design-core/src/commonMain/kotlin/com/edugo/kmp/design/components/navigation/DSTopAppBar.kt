package com.edugo.kmp.design.components.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.DSTheme
import com.edugo.kmp.design.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class DSTopAppBarVariant { SMALL, CENTER_ALIGNED, MEDIUM, LARGE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    variant: DSTopAppBarVariant = DSTopAppBarVariant.SMALL,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    when (variant) {
        DSTopAppBarVariant.SMALL ->
            TopAppBar(
                title = { Text(title) },
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actions,
                scrollBehavior = scrollBehavior,
            )
        DSTopAppBarVariant.CENTER_ALIGNED ->
            CenterAlignedTopAppBar(
                title = { Text(title) },
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actions,
                scrollBehavior = scrollBehavior,
            )
        DSTopAppBarVariant.MEDIUM ->
            MediumTopAppBar(
                title = { Text(title) },
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actions,
                scrollBehavior = scrollBehavior,
            )
        DSTopAppBarVariant.LARGE ->
            LargeTopAppBar(
                title = { Text(title) },
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actions,
                scrollBehavior = scrollBehavior,
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "DSTopAppBar - Light", showBackground = true)
@Composable
fun DSTopAppBarPreviewLight() {
    DSTheme {
        Surface {
            Column {
                DSTopAppBar(title = "Small", variant = DSTopAppBarVariant.SMALL)
                Spacer(Modifier.height(Spacing.spacing2))
                DSTopAppBar(title = "Center", variant = DSTopAppBarVariant.CENTER_ALIGNED)
                Spacer(Modifier.height(Spacing.spacing2))
                DSTopAppBar(title = "Medium", variant = DSTopAppBarVariant.MEDIUM)
                Spacer(Modifier.height(Spacing.spacing2))
                DSTopAppBar(title = "Large", variant = DSTopAppBarVariant.LARGE)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "DSTopAppBar - Dark", showBackground = true)
@Composable
private fun DSTopAppBarPreviewDark() {
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            Column {
                DSTopAppBar(title = "Small", variant = DSTopAppBarVariant.SMALL)
                Spacer(Modifier.height(Spacing.spacing2))
                DSTopAppBar(title = "Center", variant = DSTopAppBarVariant.CENTER_ALIGNED)
                Spacer(Modifier.height(Spacing.spacing2))
                DSTopAppBar(title = "Medium", variant = DSTopAppBarVariant.MEDIUM)
                Spacer(Modifier.height(Spacing.spacing2))
                DSTopAppBar(title = "Large", variant = DSTopAppBarVariant.LARGE)
            }
        }
    }
}
