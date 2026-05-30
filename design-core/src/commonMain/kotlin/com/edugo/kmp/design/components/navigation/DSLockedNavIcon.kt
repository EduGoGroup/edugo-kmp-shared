package com.edugo.kmp.design.components.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.tokens.IconCatalog

/**
 * Opacidad aplicada a un item de navegación bloqueado. M3 usa ~0.38 para estados
 * deshabilitados; lo replicamos para que el item se vea atenuado sin perder el
 * click (el consumidor intercepta el tap para resolver el contexto faltante).
 * Compartida por las tres bandas (rail / bottom-nav / drawer).
 */
internal const val DisabledNavItemAlpha = 0.38f

/**
 * Envuelve el icono de un item de navegación y, cuando [locked] es `true`,
 * superpone un pequeño candado en la esquina inferior derecha.
 *
 * Indica visualmente que el destino está bloqueado por falta de contexto
 * (colegio/unidad). Es la pieza compartida que usan las tres bandas
 * (rail / bottom-nav / drawer) para no duplicar el badge de bloqueo.
 * El icono de candado se resuelve desde el catálogo neutral del design system
 * ([IconCatalog]); si no estuviera registrado, no se pinta indicador.
 */
@Composable
fun DSLockedNavIcon(
    locked: Boolean,
    content: @Composable () -> Unit,
) {
    if (!locked) {
        content()
        return
    }
    Box(contentAlignment = Alignment.Center) {
        content()
        val lock = IconCatalog.lookup("lock", filled = true)
        if (lock != null) {
            Icon(
                imageVector = lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 6.dp)
                    .size(12.dp),
            )
        }
    }
}
