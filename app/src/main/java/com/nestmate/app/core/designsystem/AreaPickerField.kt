package com.nestmate.app.core.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.nestmate.app.core.common.Locations

/**
 * Validated area autocomplete backed by the curated [Locations] catalog.
 * The user types to filter; picking a suggestion guarantees a valid, typo-free
 * area (and its city is derivable via [Locations.cityForArea]).
 *
 * [isError] surfaces when the current text isn't a known area, so callers can
 * validate before submit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaPickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Area",
    isError: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val suggestions = remember(value) {
        if (value.isBlank()) Locations.allAreas
        else Locations.allAreas.filter { it.contains(value.trim(), ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            leadingIcon = leadingIcon,
            singleLine = true,
            isError = isError,
            supportingText = if (isError) {
                { Text("Pick a listed area", color = MaterialTheme.colorScheme.error) }
            } else null,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            suggestions.take(30).forEach { area ->
                DropdownMenuItem(
                    text = { Text("$area${Locations.cityForArea(area)?.let { ", $it" } ?: ""}") },
                    onClick = {
                        onValueChange(area)
                        expanded = false
                    }
                )
            }
        }
    }
}
