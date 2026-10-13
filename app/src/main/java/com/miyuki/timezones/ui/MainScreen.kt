package com.miyuki.timezones.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miyuki.timezones.data.CustomField
import com.miyuki.timezones.data.DatabaseProvider
import com.miyuki.timezones.data.Person
import com.miyuki.timezones.data.Timezone
import com.miyuki.timezones.repository.PeopleRepository
import com.miyuki.timezones.ui.components.PersonDetailsDialog
import com.miyuki.timezones.ui.components.PersonEditorDialog
import com.miyuki.timezones.ui.components.TimezoneReorderDialog
import com.miyuki.timezones.viewmodel.PeopleViewModel
import com.miyuki.timezones.viewmodel.PeopleViewModelFactory
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds
import android.text.format.DateFormat as AndroidDateFormat

private enum class MainTab {
    Time, People,
}

@Composable
fun MainScreen() {
    val context = LocalContext.current

    val peopleRepository = remember {
        PeopleRepository(DatabaseProvider.get(context))
    }
    val viewModel: PeopleViewModel = viewModel(
        factory = remember(peopleRepository) {
            PeopleViewModelFactory(peopleRepository)
        },
    )

    val people by viewModel.people.collectAsState()
    val timezones by viewModel.timezones.collectAsState()
    val customFieldNames by viewModel.customFieldNames.collectAsState()

    var selectedTab by remember { mutableStateOf(MainTab.Time) }
    var showAddPerson by remember { mutableStateOf(false) }
    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    var showEditPerson by remember { mutableStateOf(false) }
    var showTimezoneReorder by remember { mutableStateOf(false) }

    val selectedPersonFields by viewModel.observeCustomFields(selectedPerson?.id ?: -1L)
        .collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            if (selectedTab == MainTab.People) {
                FloatingActionButton(
                    onClick = { showAddPerson = true }) {
                    Icon(
                        imageVector = Icons.Default.Add, contentDescription = "Add Person"
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.Time,
                    onClick = { selectedTab = MainTab.Time },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                        )
                    },
                    label = { Text("Time") },
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.People,
                    onClick = { selectedTab = MainTab.People },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                        )
                    },
                    label = { Text("People") },
                )
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.Time -> TimeScreen(
                people = people,
                timezones = timezones,
                modifier = Modifier.padding(innerPadding),
            )

            MainTab.People -> PeopleScreen(
                people = people,
                timezones = timezones,
                modifier = Modifier.padding(innerPadding),
                onPersonClick = {
                    selectedPerson = it
                    showEditPerson = false
                },
                onReorderTimezones = { showTimezoneReorder = true })
        }
    }

    if (showAddPerson) {
        PersonEditorDialog(
            fieldNameSuggestions = customFieldNames,
            timezones = timezones,
            onDismiss = {
                showAddPerson = false
            },
            onSave = {
                    name,
                    nickname,
                    timezoneId,
                    newTimezoneId,
                    newTimezoneName,
                    customFields,
                ->
                val fields = customFields.map { (fieldName, fieldValue) ->
                    CustomField(
                        personId = 0,
                        name = fieldName,
                        value = fieldValue,
                    )
                }

                if (timezoneId != null) {
                    viewModel.addPerson(
                        name = name,
                        nickname = nickname.ifBlank { null },
                        timezoneId = timezoneId,
                        customFields = fields,
                    )
                    showAddPerson = false
                } else {
                    viewModel.addTimezone(
                        ianaId = newTimezoneId,
                        displayName = newTimezoneName,
                    ) { createdTimezoneId ->
                        viewModel.addPerson(
                            name = name,
                            nickname = nickname.ifBlank { null },
                            timezoneId = createdTimezoneId,
                            customFields = fields,
                        )
                        showAddPerson = false
                    }
                }
            },
        )
    }


    val personToEdit = selectedPerson

    if (personToEdit != null && !showEditPerson) {
        PersonDetailsDialog(
            person = personToEdit,
            timezone = timezones.firstOrNull {
                it.id == personToEdit.timezoneId
            },
            customFields = selectedPersonFields,
            onDismiss = {
                selectedPerson = null
            },
            onEdit = {
                showEditPerson = true
            },
        )
    }

    if (personToEdit != null && showEditPerson) {
        PersonEditorDialog(
            timezones = timezones,
            fieldNameSuggestions = customFieldNames,
            person = personToEdit,
            initialFields = selectedPersonFields,
            onDismiss = {
                showEditPerson = false
            },
            onSave = { _, _, _, _, _, _ ->
                // This callback is used only for adding.
            },
            onSaveEdit = { updatedPerson, updatedFields, newTimezoneId, newTimezoneName ->
                if (newTimezoneId != null && newTimezoneName != null) {
                    viewModel.addTimezone(
                        ianaId = newTimezoneId,
                        displayName = newTimezoneName,
                    ) { createdTimezoneId ->
                        viewModel.updatePerson(
                            updatedPerson.copy(timezoneId = createdTimezoneId),
                            updatedFields,
                        )
                        selectedPerson = null
                        showEditPerson = false
                    }
                } else {
                    viewModel.updatePerson(updatedPerson, updatedFields)
                    selectedPerson = null
                    showEditPerson = false
                }
            },
            onDelete = { person ->
                viewModel.deletePerson(person)
                selectedPerson = null
                showEditPerson = false
            },
        )
    }

    if (showTimezoneReorder) {
        TimezoneReorderDialog(timezones = timezones, onDismiss = {
            showTimezoneReorder = false
        }, onSaveOrder = { orderedIds ->
            viewModel.reorderTimezones(orderedIds)
            showTimezoneReorder = false
        })
    }
}

@Composable
private fun TimeScreen(
    people: List<Person>,
    timezones: List<Timezone>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    var currentTime by remember {
        mutableStateOf(ZonedDateTime.now())
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = ZonedDateTime.now()
            delay(1_000.milliseconds)
        }
    }

    fun formatTime(dateTime: ZonedDateTime): String {
        return AndroidDateFormat.getTimeFormat(context).apply {
            timeZone = java.util.TimeZone.getTimeZone(dateTime.zone)
        }.format(Date.from(dateTime.toInstant()))
    }

    fun getOrdinalSuffix(day: Int): String {
        return when {
            day % 100 in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
    }

    val ordinalSuffix = getOrdinalSuffix(LocalDate.now().dayOfMonth)
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d'${ordinalSuffix}' MMMM yyyy")

    val localTime = formatTime(currentTime)
    val localDate = currentTime.format(dateFormatter)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.size(28.dp))

        Text(
            text = localTime,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Medium,
        )

        Text(
            text = localDate,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.size(28.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = timezones.filter { timezone ->
                    val zoneTime = formatTime(
                        currentTime.withZoneSameInstant(
                            ZoneId.of(timezone.ianaId),
                        ),
                    )

                    zoneTime != localTime && people.any { it.timezoneId == timezone.id }
                },
                key = { it.id },
            ) { timezone ->
                val zoneTime = currentTime.withZoneSameInstant(
                    ZoneId.of(timezone.ianaId),
                )

                val timezonePeople = people.filter {
                    it.timezoneId == timezone.id
                }

                TimezoneCard(
                    timezone = timezone,
                    time = formatTime(zoneTime),
                    people = timezonePeople,
                )
            }
        }
    }
}

@Composable
private fun TimezoneCard(
    timezone: Timezone,
    time: String,
    people: List<Person>,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = timezone.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            people.forEach { person ->
                Text(
                    text = person.nickname?.takeIf { it.isNotBlank() } ?: person.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PeopleScreen(
    people: List<Person>,
    timezones: List<Timezone>,
    modifier: Modifier = Modifier,
    onPersonClick: (Person) -> Unit,
    onReorderTimezones: () -> Unit,
) {
    val context = LocalContext.current

    var currentTime by remember {
        mutableStateOf(ZonedDateTime.now())
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = ZonedDateTime.now()
            delay(1_000.milliseconds)
        }
    }

    fun formatPersonTime(timezone: Timezone?): String {
        if (timezone == null) return "—"

        return AndroidDateFormat.getTimeFormat(context).apply {
            timeZone = java.util.TimeZone.getTimeZone(
                ZoneId.of(timezone.ianaId),
            )
        }.format(Date.from(currentTime.toInstant()))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.size(28.dp))

        Text(
            text = "People",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.size(12.dp))

        OutlinedButton(
            onClick = onReorderTimezones,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Reorder timezones")
        }

        Spacer(Modifier.size(12.dp))

        if (people.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "No people yet",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Text(
                        text = "Add someone to start tracking their local time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = people.sortedBy { it.name.lowercase() },
                    key = { it.id },
                ) { person ->
                    val timezone = timezones.firstOrNull {
                        it.id == person.timezoneId
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPersonClick(person)
                            },
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = person.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                )

                                if (!person.nickname.isNullOrBlank()) {
                                    Text(
                                        text = person.nickname,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            Spacer(Modifier.size(12.dp))

                            Text(
                                text = formatPersonTime(timezone),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}