package com.edugo.kmp.design.components.lists.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.lists.RowAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwipeActionsStrategy(
    actions: List<RowAction>,
    onAction: (RowAction) -> Unit,
    modifier: Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    val destructive = actions.firstOrNull { it.destructive }
    val primary = actions.firstOrNull { !it.destructive }

    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                when (value) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        destructive?.let { onAction(it) }
                        false
                    }
                    SwipeToDismissBoxValue.StartToEnd -> {
                        primary?.let { onAction(it) }
                        false
                    }
                    SwipeToDismissBoxValue.Settled -> true
                }
            },
        )

    // Lectores de pantalla descubren las acciones disponibles vía customActions
    // (no requieren gestos físicos de swipe); stateDescription anuncia la acción
    // inminente cuando el swipe está en curso.
    val a11yActions =
        listOfNotNull(
            primary?.let { action ->
                CustomAccessibilityAction(label = action.label, action = {
                    onAction(action)
                    true
                })
            },
            destructive?.let { action ->
                CustomAccessibilityAction(label = action.label, action = {
                    onAction(action)
                    true
                })
            },
        )
    val swipeStateDescription =
        when (dismissState.targetValue) {
            SwipeToDismissBoxValue.EndToStart -> destructive?.label.orEmpty()
            SwipeToDismissBoxValue.StartToEnd -> primary?.label.orEmpty()
            SwipeToDismissBoxValue.Settled -> ""
        }

    SwipeToDismissBox(
        state = dismissState,
        modifier =
            modifier.semantics {
                customActions = a11yActions
                if (swipeStateDescription.isNotEmpty()) {
                    stateDescription = swipeStateDescription
                }
            },
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val (bgColor, icon, iconDescription, alignment) =
                when (direction) {
                    SwipeToDismissBoxValue.EndToStart ->
                        SwipeBackground(
                            color = MaterialTheme.colorScheme.errorContainer,
                            icon = Icons.Filled.Delete,
                            description = destructive?.label ?: "",
                            alignment = Alignment.CenterEnd,
                        )
                    SwipeToDismissBoxValue.StartToEnd ->
                        SwipeBackground(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            icon = primary?.icon ?: Icons.Filled.Delete,
                            description = primary?.label ?: "",
                            alignment = Alignment.CenterStart,
                        )
                    SwipeToDismissBoxValue.Settled ->
                        SwipeBackground(
                            color = MaterialTheme.colorScheme.surface,
                            icon = Icons.Filled.Delete,
                            description = "",
                            alignment = Alignment.CenterEnd,
                        )
                }
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(bgColor)
                        .padding(horizontal = Spacing.spacing4),
                contentAlignment = alignment,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconDescription,
                        tint =
                            when (direction) {
                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
                                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onSecondaryContainer
                                SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.onSurface
                            },
                    )
                }
            }
        },
        enableDismissFromStartToEnd = primary != null,
        enableDismissFromEndToStart = destructive != null,
    ) {
        content(Modifier)
    }
}

private data class SwipeBackground(
    val color: androidx.compose.ui.graphics.Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String,
    val alignment: Alignment,
)
