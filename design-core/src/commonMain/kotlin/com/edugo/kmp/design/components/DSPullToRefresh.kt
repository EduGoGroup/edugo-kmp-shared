package com.edugo.kmp.design.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Design-system wrapper for pull-to-refresh. Occulta la API de Material3
 * [PullToRefreshBox] y ofrece un contrato estable para los renderers de
 * pantalla (LIST, DASHBOARD, etc.).
 *
 * Uso:
 * ```kotlin
 * DSPullToRefresh(
 *     isRefreshing = isRevalidating,
 *     onRefresh = onReload,
 * ) {
 *     // contenido scrollable aquí
 * }
 * ```
 *
 * [isRefreshing] debe ser `true` únicamente mientras el fetch está en vuelo;
 * usa `dataState.isRevalidating` (F2) o un flag local según el contexto.
 * NO usar `observer.isOnline` como gate.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        content()
    }
}
