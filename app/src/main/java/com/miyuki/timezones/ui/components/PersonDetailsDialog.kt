package com.miyuki.timezones.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import com.miyuki.timezones.R
import com.miyuki.timezones.data.CustomField
import com.miyuki.timezones.data.Person
import com.miyuki.timezones.data.Timezone
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun PersonDetailsDialog(
    person: Person,
    timezone: Timezone?,
    customFields: List<CustomField>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(28.dp)
    // Use the same phase palette as the Time screen, derived from this person's timezone.
    val zone = remember(timezone?.ianaId) {
        runCatching { ZoneId.of(timezone?.ianaId ?: ZoneId.systemDefault().id) }
            .getOrDefault(ZoneId.systemDefault())
    }
    val phaseColor = personPhaseColor(ZonedDateTime.now(zone).hour * 60 + ZonedDateTime.now(zone).minute)
    val animatedPhase by animateColorAsState(phaseColor, tween(650), label = "Person phase")
    val base = colors.surface
    val outline = Brush.linearGradient(
        colors = listOf(animatedPhase.copy(alpha = 0.85f), animatedPhase.copy(alpha = 0.25f), animatedPhase.copy(alpha = 0.65f)),
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 460.dp)
                .background(base, shape)
                .background(
                    Brush.verticalGradient(
                        0f to animatedPhase.copy(alpha = 0.22f),
                        0.48f to animatedPhase.copy(alpha = 0.07f),
                        1f to Color.Transparent,
                    ), shape,
                )
                .border(1.dp, outline, shape)
                .padding(22.dp),
        ) {
            Column {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!person.nickname.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = person.nickname,
                        style = MaterialTheme.typography.titleMedium,
                        color = animatedPhase,
                    )
                }
                Spacer(Modifier.height(22.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (timezone != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(animatedPhase.copy(alpha = 0.10f), RoundedCornerShape(18.dp))
                                .border(1.dp, animatedPhase.copy(alpha = 0.40f), RoundedCornerShape(18.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.timezone),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurfaceVariant,
                            )
                            Text(
                                text = timezone.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurface,
                            )
                            Text(
                                text = timezone.ianaId,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }

                    customFields.forEach { field ->
                        DetailCard(field.name, field.value, animatedPhase)
                    }
                    if (timezone == null && customFields.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_additional_details),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, animatedPhase.copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface),
                    ) {
                        Text(stringResource(R.string.close))
                    }
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = animatedPhase.copy(alpha = 0.23f),
                            contentColor = colors.onSurface,
                        ),
                    ) {
                        Text(stringResource(R.string.edit_details))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(label: String, value: String, accent: Color) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainer.copy(alpha = 0.65f), shape)
            .border(1.dp, accent.copy(alpha = 0.22f), shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.85f),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value.ifBlank { "—" },
            modifier = Modifier.weight(1.15f),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Medium,
        )
    }
}

// Matches the phase color ranges currently defined in MainScreen.kt.
private fun personPhaseColor(minute: Int): Color = when (minute) {
    in 0 until 180 -> Color(0xFF344A91)
    in 180 until 300 -> Color(0xFF7868B2)
    in 300 until 360 -> Color(0xFFC18AC2)
    in 360 until 420 -> Color(0xFFF49A91)
    in 420 until 540 -> Color(0xFFF7B36D)
    in 540 until 660 -> Color(0xFFF7CF69)
    in 660 until 780 -> Color(0xFFFFE66D)
    in 780 until 960 -> Color(0xFFF4BB83)
    in 960 until 1080 -> Color(0xFFE99480)
    in 1080 until 1140 -> Color(0xFFD978A7)
    in 1140 until 1200 -> Color(0xFFA779BF)
    in 1200 until 1260 -> Color(0xFF718AC1)
    else -> Color(0xFF5067A7)
}
