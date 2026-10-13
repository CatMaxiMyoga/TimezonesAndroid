
package com.miyuki.timezones.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
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
import androidx.compose.ui.unit.dp
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

    var selectedTimezoneId by remember(person?.id, timezones) {
        mutableStateOf(
            person?.timezoneId ?: timezones.firstOrNull()?.id,
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

    var errorMessage by remember { mutableStateOf<String?>(null) }
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
            title = { Text("Delete ${person.name}?") },
            text = {
                Text(
                    "This permanently deletes this person and their custom fields. " +
                            "Their timezone will be removed if nobody else uses it.",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete?.invoke(person)
                    },
                ) {
                    Text("Delete Person")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(if (person == null) "Add Person" else "Edit Person")
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
                        label = { Text("Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("Nickname") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text("Timezone")

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
                                    }?.displayName ?: "Choose Timezone",
                                )
                            }

                            DropdownMenu(
                                expanded = timezoneMenuExpanded,
                                onDismissRequest = {
                                    timezoneMenuExpanded = false
                                },
                            ) {
                                timezones.forEach { timezone ->
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
                            label = { Text("Timezone Display Name *") },
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
                                label = { Text("IANA Timezone ID *") },
                                placeholder = { Text("Search, e.g. Berlin") },
                                singleLine = true,
                                isError = newTimezoneId.isNotBlank() &&
                                        !isValidTimezone(newTimezoneId.trim()),
                                supportingText = {
                                    if (
                                        newTimezoneId.isNotBlank() &&
                                        !isValidTimezone(newTimezoneId.trim())
                                    ) {
                                        Text("Choose a valid timezone ID.")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(
                                        type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                                        enabled = true
                                    ),
                            )

                            val matchingTimezones = remember(newTimezoneId) {
                                val query = newTimezoneId.trim()

                                if (query.isBlank()) {
                                    availableTimezoneIds.take(8)
                                } else {
                                    availableTimezoneIds
                                        .filter {
                                            it.contains(
                                                query,
                                                ignoreCase = true,
                                            )
                                        }
                                        .take(8)
                                }
                            }

                            ExposedDropdownMenu(
                                expanded = timezoneSuggestionsExpanded &&
                                        matchingTimezones.isNotEmpty(),
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
                                                newTimezoneName = timezoneId
                                                    .substringAfterLast('/')
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
                                    selectedTimezoneId =
                                        timezones.firstOrNull()?.id
                                    timezoneSuggestionsExpanded = false
                                    errorMessage = null
                                },
                            ) {
                                Text("Choose an existing Timezone")
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
                            Text("Create a new Timezone")
                        }
                    }

                    customFields.forEachIndexed { index, field ->
                        HorizontalDivider(
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box {
                                OutlinedTextField(
                                    value = field.name,
                                    onValueChange = { newName ->
                                        customFields[index] = field.copy(
                                            name = newName,
                                            suggestionsExpanded = true,
                                        )
                                    },
                                    label = { Text("Field Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                val matches = fieldNameSuggestions
                                    .filter {
                                        it.contains(
                                            field.name,
                                            ignoreCase = true,
                                        ) && !it.equals(
                                            field.name,
                                            ignoreCase = true,
                                        )
                                    }
                                    .take(6)

                                DropdownMenu(
                                    expanded = field.suggestionsExpanded &&
                                            field.name.isNotBlank() &&
                                            matches.isNotEmpty(),
                                    onDismissRequest = {
                                        customFields[index] = field.copy(
                                            suggestionsExpanded = false,
                                        )
                                    },
                                ) {
                                    matches.forEach { suggestion ->
                                        DropdownMenuItem(
                                            text = { Text(suggestion) },
                                            onClick = {
                                                customFields[index] = field.copy(
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
                                label = { Text("Value") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            TextButton(
                                onClick = {
                                    customFields.removeAt(index)
                                },
                            ) {
                                Text("Remove Field")
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            customFields.add(CustomFieldDraft())
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Add Custom Field")
                    }

                    if (errorMessage != null) {
                        Text(errorMessage!!)
                    }

                    if (person != null && onDelete != null) {
                        TextButton(
                            onClick = {
                                showDeleteConfirmation = true
                            },
                        ) {
                            Text("Delete Person")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        errorMessage = when {
                            name.isBlank() ->
                                "Enter a Name."

                            creatingTimezone &&
                                    newTimezoneName.isBlank() ->
                                "Enter a Timezone Display Name."

                            creatingTimezone &&
                                    newTimezoneId.isBlank() ->
                                "Choose an IANA Timezone ID."

                            creatingTimezone &&
                                    !isValidTimezone(newTimezoneId.trim()) ->
                                "Choose a valid IANA Timezone ID."

                            !creatingTimezone &&
                                    selectedTimezoneId == null ->
                                "Choose a Timezone."

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
                                        it.first.isNotEmpty() ||
                                                it.second.isNotEmpty()
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

                                val updatedFields = customFields
                                    .filter {
                                        it.name.isNotBlank() || it.value.isNotBlank()
                                    }
                                    .map {
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
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            },
        )
    }
}

private fun isValidTimezone(ianaId: String): Boolean {
    return ianaId in availableTimezoneIds
}