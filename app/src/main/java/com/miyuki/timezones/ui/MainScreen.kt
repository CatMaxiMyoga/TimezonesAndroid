package com.miyuki.timezones.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miyuki.timezones.R
import com.miyuki.timezones.data.CustomField
import com.miyuki.timezones.data.DatabaseProvider
import com.miyuki.timezones.data.Person
import com.miyuki.timezones.data.Timezone
import com.miyuki.timezones.repository.PeopleRepository
import com.miyuki.timezones.ui.components.TimeComparisonDialog
import com.miyuki.timezones.ui.components.PersonDetailsDialog
import com.miyuki.timezones.ui.components.PersonEditorDialog
import com.miyuki.timezones.ui.components.TimezoneReorderDialog
import com.miyuki.timezones.viewmodel.PeopleViewModel
import com.miyuki.timezones.viewmodel.PeopleViewModelFactory
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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
    var comparisonTimezone by remember { mutableStateOf<Timezone?>(null) }

    val selectedPersonFields by viewModel.observeCustomFields(selectedPerson?.id ?: -1L)
        .collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            if (selectedTab == MainTab.People) {
                FloatingActionButton(
                    onClick = { showAddPerson = true }) {
                    Icon(
                        imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_person)
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
                    label = { Text(stringResource(R.string.time)) },
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
                    label = { Text(stringResource(R.string.people)) },
                )
            }
        },
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.Time -> TimeScreen(
                people = people,
                timezones = timezones,
                modifier = Modifier.padding(innerPadding),
                onPersonClick = { selectedPerson = it; showEditPerson = false },
                onTimezoneTimeClick = { comparisonTimezone = it },
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

    comparisonTimezone?.let { timezone ->
        TimeComparisonDialog(timezone = timezone, onDismiss = { comparisonTimezone = null })
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
            onSave = { _, _, _, _, _, _ -> },
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
    onPersonClick: (Person) -> Unit,
    onTimezoneTimeClick: (Timezone) -> Unit,
) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

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

    fun formatDate(dateTime: ZonedDateTime): String {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(locale)
        return dateTime.format(formatter)
    }

    val localPhase = getDayPhase(currentTime)
    val localTime = formatTime(currentTime)
    val localDate = formatDate(currentTime)

    val localTimezone = timezones.firstOrNull {
        it.ianaId == currentTime.zone.id
    }

    val localPeople = people.filter {
        it.timezoneId == localTimezone?.id
    }

    val otherTimezones = timezones.filter { timezone ->
        timezone.ianaId != currentTime.zone.id &&
                people.any { it.timezoneId == timezone.id }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // The phase color fades back into the normal background
            // before the timezone cards begin.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to localPhase.color.copy(alpha = 0.22f),
                                0.5f to localPhase.color.copy(alpha = 0.22f),
                                1.0f to backgroundColor,
                            ),
                        ),
                    )
                    .padding(horizontal = 20.dp)
                    .padding(top = 28.dp, bottom = 20.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Local phase above the clock.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = localPhase.icon,
                            style = MaterialTheme.typography.titleLarge,
                            color = localPhase.color,
                        )

                        Text(
                            text = stringResource(localPhase.labelRes),
                            style = MaterialTheme.typography.titleMedium,
                            color = localPhase.color,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Text(
                        text = localTime,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                    )

                    Text(
                        text = localDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryTextColor,
                    )

                    // People sharing your local timezone.
                    if (localPeople.isNotEmpty()) {
                        Spacer(Modifier.size(8.dp))

                        PeopleChips(
                            people = localPeople,
                            accentColor = localPhase.color,
                            onPersonClick = onPersonClick,
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 4.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = otherTimezones,
                    key = { it.id },
                ) { timezone ->
                    val zoneTime = currentTime.withZoneSameInstant(
                        ZoneId.of(timezone.ianaId),
                    )

                    val phase = getDayPhase(zoneTime)

                    TimezoneCard(
                        timezone = timezone,
                        time = formatTime(zoneTime),
                        date = formatDate(zoneTime),
                        phase = phase,
                        onPersonClick = onPersonClick,
                        onTimeClick = { onTimezoneTimeClick(timezone) },
                        people = people.filter {
                            it.timezoneId == timezone.id
                        },
                    )
                }
            }
        }
    }
}

internal data class DayPhase(
    @StringRes val labelRes: Int,
    val icon: String,
    val color: Color,
)

internal fun getDayPhase(time: ZonedDateTime): DayPhase {
    val minuteOfDay = time.hour * 60 + time.minute

    return when (minuteOfDay) {
        in 0 until 180 -> DayPhase(
            R.string.deep_night, "✦", Color(0xFF344A91),
        )

        in 180 until 300 -> DayPhase(
            R.string.pre_dawn, "☾", Color(0xFF7868B2),
        )

        in 300 until 360 -> DayPhase(
            R.string.dawn, "☼", Color(0xFFC18AC2),
        )

        in 360 until 420 -> DayPhase(
            R.string.sunrise, "☼", Color(0xFFF49A91),
        )

        in 420 until 540 -> DayPhase(
            R.string.morning, "☼", Color(0xFFF7B36D),
        )

        in 540 until 660 -> DayPhase(
            R.string.late_morning, "☼", Color(0xFFF7CF69),
        )

        in 660 until 780 -> DayPhase(
            R.string.noon, "☼", Color(0xFFFFE66D),
        )

        in 780 until 960 -> DayPhase(
            R.string.afternoon, "☼", Color(0xFFF4BB83),
        )

        in 960 until 1080 -> DayPhase(
            R.string.late_afternoon, "☼", Color(0xFFE99480),
        )

        in 1080 until 1140 -> DayPhase(
            R.string.sunset, "☼", Color(0xFFD978A7),
        )

        in 1140 until 1200 -> DayPhase(
            R.string.dusk, "☾", Color(0xFFA779BF),
        )

        in 1200 until 1260 -> DayPhase(
            R.string.evening, "☾", Color(0xFF718AC1),
        )

        else -> DayPhase(
            R.string.late_night, "☾", Color(0xFF5067A7),
        )
    }
}

@Composable
private fun TimezoneCard(
    timezone: Timezone,
    time: String,
    date: String,
    phase: DayPhase,
    people: List<Person>,
    onPersonClick: (Person) -> Unit,
    onTimeClick: () -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        phase.color.copy(alpha = 0.20f),
                        backgroundColor,
                    ),
                    start = Offset.Zero,
                    end = Offset(900f, 600f),
                ),
                shape = shape,
            )
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    color = phase.color.copy(alpha = 0.65f),
                ),
                shape = shape,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Row 1: timezone name on the left, time on the right.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = timezone.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.weight(1f, fill = false),
            )

            Spacer(Modifier.size(8.dp))

            Text(
                text = time,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.clickable(onClick = onTimeClick),
            )
        }

        // Row 2: date on the left, phase on the right.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = secondaryTextColor,
                modifier = Modifier.weight(1f, fill = false),
            )

            Spacer(Modifier.size(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = phase.icon,
                    style = MaterialTheme.typography.bodyMedium,
                    color = phase.color,
                )

                Text(
                    text = stringResource(phase.labelRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = phase.color,
                )
            }
        }

        // Row 3+: outlined person chips that wrap as complete units.
        if (people.isNotEmpty()) {
            PeopleChips(
                people = people,
                accentColor = phase.color,
                onPersonClick = onPersonClick,
            )
        }
    }
}

@Composable
private fun PeopleChips(
    people: List<Person>,
    accentColor: Color,
    onPersonClick: (Person) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        people.forEach { person ->
            val displayName = person.nickname
                ?.takeIf { it.isNotBlank() }
                ?: person.name

            Box(
                modifier = Modifier
                    .background(
                        color = accentColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                    )
                    .border(
                        width = 1.dp,
                        color = accentColor.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(10.dp),
                    )
                    .clickable { onPersonClick(person) }
                    .padding(
                        horizontal = 10.dp,
                        vertical = 6.dp,
                    ),
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
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
            text = stringResource(R.string.people),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.size(12.dp))

        OutlinedButton(
            onClick = onReorderTimezones,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.reorder_timezones))
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
                        text = stringResource(R.string.no_people_yet),
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Text(
                        text = stringResource(R.string.add_someone_hint),
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

