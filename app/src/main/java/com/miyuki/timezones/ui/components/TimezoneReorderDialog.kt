package com.miyuki.timezones.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miyuki.timezones.data.Timezone
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun TimezoneReorderDialog(
    timezones: List<Timezone>,
    onDismiss: () -> Unit,
    onSaveOrder: (List<Long>) -> Unit,
) {
    var orderedTimezones by remember(timezones) {
        mutableStateOf(timezones.sortedBy { it.order })
    }

    val lazyListState = rememberLazyListState()

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
    ) { from, to ->
        orderedTimezones = orderedTimezones.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reorder Timezones") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Drag the handle beside a Timezone to change its position.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                LazyColumn(
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = orderedTimezones,
                        key = { it.id },
                    ) { timezone ->
                        ReorderableItem(
                            state = reorderableState,
                            key = timezone.id,
                        ) { isDragging ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shadowElevation = if (isDragging) 4.dp else 0.dp,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = timezone.displayName,
                                        modifier = Modifier.weight(1f),
                                    )

                                    IconButton(
                                        modifier = this@ReorderableItem.run {
                                            Modifier.draggableHandle()
                                        },
                                        onClick = {},
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DragHandle,
                                            contentDescription = "Drag to reorder",
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSaveOrder(orderedTimezones.map { it.id })
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