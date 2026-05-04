package com.edugo.kmp.design.components.inputs

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.DSTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DSFilledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null) } },
        trailingIcon = trailingIcon?.let { icon -> { Icon(icon, contentDescription = null) } },
        isError = isError,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}

@Preview(name = "DSFilledTextField - Light", showBackground = true)
@Composable
fun DSFilledTextFieldPreviewLight() {
    var value by remember { mutableStateOf("Ejemplo") }
    DSTheme {
        Surface {
            DSFilledTextField(
                value = value,
                onValueChange = { value = it },
                label = "Nombre",
                placeholder = "Ingresa tu nombre",
            )
        }
    }
}

@Preview(name = "DSFilledTextField - Dark", showBackground = true)
@Composable
private fun DSFilledTextFieldPreviewDark() {
    var value by remember { mutableStateOf("Ejemplo") }
    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSFilledTextField(
                value = value,
                onValueChange = { value = it },
                label = "Nombre",
                placeholder = "Ingresa tu nombre",
            )
        }
    }
}
