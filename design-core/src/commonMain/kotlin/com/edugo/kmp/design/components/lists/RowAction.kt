package com.edugo.kmp.design.components.lists

import androidx.compose.ui.graphics.vector.ImageVector

data class RowAction(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val destructive: Boolean = false,
    val onInvoke: () -> Unit,
)
