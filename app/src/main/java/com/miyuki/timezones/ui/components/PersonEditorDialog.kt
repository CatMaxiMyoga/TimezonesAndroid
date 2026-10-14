package com.miyuki.timezones.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.miyuki.timezones.R
import com.miyuki.timezones.data.CustomField
import com.miyuki.timezones.data.Person
import com.miyuki.timezones.data.Timezone
import java.time.ZoneId

private data class CustomFieldDraft(
    val id: Long? = null,
    val name: String = "",
    val value: String = "",
    val suggestionsExpanded: Boolean = false,
)

private val availableTimezoneIds = ZoneId.getAvailableZoneIds().sorted()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonEditorDialog(
    timezones: List<Timezone>,
    fieldNameSuggestions: List<String>,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        nickname: String,
        timezoneId: Long?,
        newTimezoneId: String,
        newTimezoneName: String,
        customFields: List<Pair<String, String>>,
    ) -> Unit,
    person: Person? = null,
    initialFields: List<CustomField> = emptyList(),
    onSaveEdit: ((Person, List<CustomField>, String?, String?) -> Unit)? = null,
    onDelete: ((Person) -> Unit)? = null,
) {
    var name by remember(person?.id) {
        mutableStateOf(person?.name.orEmpty())
    }
    var nickname by remember(person?.id) {
        mutableStateOf(person?.nickname.orEmpty())
    }

    val prioritizedTimezones = remember(timezones) {
        timezones.sortedWith(compareBy<Timezone> { it.ianaId != ZoneId.systemDefault().id }.thenBy { it.order })
    }

    var selectedTimezoneId by remember(person?.id, prioritizedTimezones) {
        mutableStateOf(
            person?.timezoneId ?: prioritizedTimezones.firstOrNull()?.id,
        )
    }
    var timezoneMenuExpanded by remember { mutableStateOf(false) }

    var creatingTimezone by remember(person?.id) {
        mutableStateOf(person == null && timezones.isEmpty())
    }
    var newTimezoneId by remember(person?.id) {
        mutableStateOf("")
    }
    var timezoneSuggestionsExpanded by remember {
        mutableStateOf(false)
    }
    var newTimezoneName by remember(person?.id) {
        mutableStateOf("")
    }

    var errorMessage by remember { mutableStateOf<Int?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val customFields = remember(person?.id, initialFields) {
        mutableStateListOf<CustomFieldDraft>().apply {
            addAll(
                initialFields.map {
                    CustomFieldDraft(
                        id = it.id,
                        name = it.name,
                        value = it.value,
                    )
                },
            )
        }
    }

    if (showDeleteConfirmation && person != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
            },
            title = { Text(stringResource(R.string.delete_person_title, person.name)) },
            text = {
                Text(
                    stringResource(R.string.delete_person_warning),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete?.invoke(person)
                    },
                ) {
                    Text(stringResource(R.string.delete_person))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                    },
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(if (person == null) stringResource(R.string.add_person) else stringResource(R.string.edit_person))
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.name_required)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text(stringResource(R.string.nickname)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(stringResource(R.string.timezone))

                    if (!creatingTimezone && timezones.isNotEmpty()) {
                        Box {
                            OutlinedButton(
                                onClick = {
                                    timezoneMenuExpanded = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    timezones.firstOrNull {
                                        it.id == selectedTimezoneId
                                    }?.displayName ?: stringResource(R.string.choose_timezone),
                                )
                            }

                            DropdownMenu(
                                expanded = timezoneMenuExpanded,
                                onDismissRequest = {
                                    timezoneMenuExpanded = false
                                },
                            ) {
                                prioritizedTimezones.forEach { timezone ->
                                    DropdownMenuItem(
                                        text = { Text(timezone.displayName) },
                                        onClick = {
                                            selectedTimezoneId = timezone.id
                                            timezoneMenuExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }

                    if (creatingTimezone) {
                        OutlinedTextField(
                            value = newTimezoneName,
                            onValueChange = { newTimezoneName = it },
                            label = { Text(stringResource(R.string.timezone_display_name_required)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        ExposedDropdownMenuBox(
                            expanded = timezoneSuggestionsExpanded,
                            onExpandedChange = {
                                timezoneSuggestionsExpanded = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = newTimezoneId,
                                onValueChange = {
                                    newTimezoneId = it
                                    timezoneSuggestionsExpanded = true
                                    errorMessage = null
                                },
                                label = { Text(stringResource(R.string.iana_timezone_id_required)) },
                                placeholder = { Text(stringResource(R.string.search_berlin)) },
                                singleLine = true,
                                isError = newTimezoneId.isNotBlank() && !isValidTimezone(
                                    newTimezoneId.trim()
                                ),
                                supportingText = {
                                    if (newTimezoneId.isNotBlank() && !isValidTimezone(newTimezoneId.trim())) {
                                        Text(stringResource(R.string.choose_valid_timezone_id))
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(
                                        type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                                        enabled = true
                                    ),
                            )

                            val matchingTimezones = remember(newTimezoneId, timezones) {
                                val existingIds = timezones.map { it.ianaId.lowercase() }.toSet()
                                val query = newTimezoneId.trim()

                                if (query.isBlank()) {
                                    availableTimezoneIds.filter { it.lowercase() !in existingIds }
                                        .take(8)
                                } else {
                                    availableTimezoneIds.filter {
                                        it.lowercase() !in existingIds && it.contains(
                                            query,
                                            ignoreCase = true,
                                        )
                                    }.take(8)
                                }
                            }

                            ExposedDropdownMenu(
                                expanded = timezoneSuggestionsExpanded && matchingTimezones.isNotEmpty(),
                                onDismissRequest = {
                                    timezoneSuggestionsExpanded = false
                                },
                            ) {
                                matchingTimezones.forEach { timezoneId ->
                                    DropdownMenuItem(
                                        text = { Text(timezoneId) },
                                        onClick = {
                                            newTimezoneId = timezoneId

                                            if (newTimezoneName.isBlank()) {
                                                newTimezoneName = timezoneId.substringAfterLast('/')
                                                    .replace('_', ' ')
                                            }

                                            timezoneSuggestionsExpanded = false
                                            errorMessage = null
                                        },
                                    )
                                }
                            }
                        }

                        if (timezones.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    creatingTimezone = false
                                    selectedTimezoneId = prioritizedTimezones.firstOrNull()?.id
                                    timezoneSuggestionsExpanded = false
                                    errorMessage = null
                                },
                            ) {
                                Text(stringResource(R.string.choose_existing_timezone))
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                creatingTimezone = true
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.create_new_timezone))
                        }
                    }

                    customFields.forEachIndexed { index, field ->
                        HorizontalDivider(
                            thickness = 2.dp, color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val usedNames = customFields.withIndex().filter { it.index != index }
                                .map { it.value.name.trim().lowercase() }.toSet()
                            val matches = fieldNameSuggestions.distinctBy { it.trim().lowercase() }
                                .filter { suggestion ->
                                    val normalized = suggestion.trim().lowercase()
                                    normalized.isNotEmpty() && normalized !in usedNames && suggestion.contains(
                                        field.name.trim(), ignoreCase = true
                                    ) && !suggestion.equals(field.name.trim(), ignoreCase = true)
                                }.take(6)

                            ExposedDropdownMenuBox(
                                expanded = field.suggestionsExpanded && matches.isNotEmpty(),
                                onExpandedChange = { expanded ->
                                    customFields[index] =
                                        customFields[index].copy(suggestionsExpanded = expanded)
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                OutlinedTextField(
                                    value = field.name,
                                    onValueChange = { newName ->
                                        customFields[index] = customFields[index].copy(
                                            name = newName,
                                            suggestionsExpanded = true,
                                        )
                                    },
                                    label = { Text(stringResource(R.string.field_name)) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                                )
                                ExposedDropdownMenu(
                                    expanded = field.suggestionsExpanded && matches.isNotEmpty(),
                                    onDismissRequest = {
                                        customFields[index] =
                                            customFields[index].copy(suggestionsExpanded = false)
                                    },
                                ) {
                                    matches.forEach { suggestion ->
                                        DropdownMenuItem(
                                            text = { Text(suggestion) },
                                            onClick = {
                                                customFields[index] = customFields[index].copy(
                                                    name = suggestion,
                                                    suggestionsExpanded = false,
                                                )
                                            },
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = field.value,
                                onValueChange = { newValue ->
                                    customFields[index] = field.copy(
                                        value = newValue,
                                    )
                                },
                                label = { Text(stringResource(R.string.value)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            TextButton(
                                onClick = {
                                    customFields.removeAt(index)
                                },
                            ) {
                                Text(stringResource(R.string.remove_field))
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            customFields.add(CustomFieldDraft())
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.add_custom_field))
                    }

                    if (errorMessage != null) {
                        Text(stringResource(errorMessage!!))
                    }

                    if (person != null && onDelete != null) {
                        TextButton(
                            onClick = {
                                showDeleteConfirmation = true
                            },
                        ) {
                            Text(stringResource(R.string.delete_person))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        errorMessage = when {
                            name.isBlank() -> R.string.error_enter_name

                            creatingTimezone && newTimezoneName.isBlank() -> R.string.error_enter_timezone_name

                            creatingTimezone && newTimezoneId.isBlank() -> R.string.error_choose_iana

                            creatingTimezone && !isValidTimezone(newTimezoneId.trim()) -> R.string.error_choose_valid_iana

                            creatingTimezone && timezones.any {
                                it.ianaId.equals(newTimezoneId.trim(), ignoreCase = true)
                            } -> R.string.error_duplicate_timezone

                            customFields.map { it.name.trim().lowercase() }
                                .filter { it.isNotEmpty() }
                                .let { it.size != it.toSet().size } -> R.string.error_duplicate_field

                            !creatingTimezone && selectedTimezoneId == null -> R.string.error_choose_timezone

                            else -> null
                        }

                        if (errorMessage == null) {
                            if (person == null) {
                                onSave(
                                    name.trim(),
                                    nickname.trim(),
                                    if (creatingTimezone) {
                                        null
                                    } else {
                                        selectedTimezoneId
                                    },
                                    newTimezoneId.trim(),
                                    newTimezoneName.trim(),
                                    customFields.map {
                                        it.name.trim() to it.value.trim()
                                    }.filter {
                                        it.first.isNotEmpty() || it.second.isNotEmpty()
                                    },
                                )
                            } else {
                                val targetTimezoneId = if (creatingTimezone) {
                                    person.timezoneId
                                } else {
                                    selectedTimezoneId!!
                                }

                                val updatedPerson = person.copy(
                                    name = name.trim(),
                                    nickname = nickname.trim().ifBlank { null },
                                    timezoneId = targetTimezoneId,
                                )

                                val updatedFields = customFields.filter {
                                    it.name.isNotBlank() || it.value.isNotBlank()
                                }.map {
                                    CustomField(
                                        id = it.id ?: 0,
                                        personId = person.id,
                                        name = it.name.trim(),
                                        value = it.value.trim(),
                                    )
                                }

                                onSaveEdit?.invoke(
                                    updatedPerson,
                                    updatedFields,
                                    newTimezoneId.trim().takeIf { creatingTimezone },
                                    newTimezoneName.trim().takeIf { creatingTimezone },
                                )
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

private fun isValidTimezone(ianaId: String): Boolean {
    return ianaId in availableTimezoneIds
}
