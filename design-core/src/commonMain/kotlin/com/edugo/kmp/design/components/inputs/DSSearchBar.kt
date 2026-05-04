package com.edugo.kmp.design.components.inputs

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector = Icons.Filled.Search,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    SearchBar(
        inputField = {
            androidx.compose.material3.SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                expanded = active,
                onExpandedChange = onActiveChange,
                placeholder = placeholder?.let { { Text(it) } },
                leadingIcon = { Icon(leadingIcon, contentDescription = "Search") },
                trailingIcon =
                    if (query.isNotEmpty()) {
                        {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    } else {
                        null
                    },
            )
        },
        expanded = active,
        onExpandedChange = onActiveChange,
        modifier = modifier,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "DSSearchBar - Light", showBackground = true)
@Composable
fun DSSearchBarPreviewLight() {
    var query by remember { mutableStateOf("Busqueda") }
    var active by remember { mutableStateOf(true) }

    DSTheme {
        Surface {
            DSSearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = {},
                active = active,
                onActiveChange = { active = it },
                placeholder = "Buscar",
            ) {
                Text("Resultado 1")
                Text("Resultado 2")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "DSSearchBar - Dark", showBackground = true)
@Composable
private fun DSSearchBarPreviewDark() {
    var query by remember { mutableStateOf("Busqueda") }
    var active by remember { mutableStateOf(true) }

    DSTheme(colorScheme = darkColorScheme()) {
        Surface {
            DSSearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = {},
                active = active,
                onActiveChange = { active = it },
                placeholder = "Buscar",
            ) {
                Text("Resultado 1")
                Text("Resultado 2")
            }
        }
    }
}
