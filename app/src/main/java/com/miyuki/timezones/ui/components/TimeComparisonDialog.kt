package com.miyuki.timezones.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.miyuki.timezones.R
import com.miyuki.timezones.data.Timezone
import com.miyuki.timezones.ui.getDayPhase
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import kotlin.math.roundToInt
import android.text.format.DateFormat as AndroidDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeComparisonDialog(timezone: Timezone, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val localZone = ZoneId.systemDefault()
    val remoteZone = remember(timezone.ianaId) { ZoneId.of(timezone.ianaId) }
    val localDate = remember(localZone) { LocalDate.now(localZone) }

    var minuteOfDay by remember(timezone.id) {
        val now = ZonedDateTime.now(localZone)
        mutableIntStateOf(((now.hour * 60 + now.minute + 15) / 30 * 30).coerceIn(0, 1440))
    }

    val local = localDate.atStartOfDay().plusMinutes(minuteOfDay.toLong()).atZone(localZone)
    val remote = local.withZoneSameInstant(remoteZone)

    fun formatTime(time: ZonedDateTime): String = AndroidDateFormat.getTimeFormat(context).apply {
        timeZone = java.util.TimeZone.getTimeZone(time.zone)
    }.format(Date.from(time.toInstant()))

    val dateFormatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)
    }

    val localPhase = getDayPhase(local)
    val remotePhase = getDayPhase(remote)
    val animatedLocalColor by animateColorAsState(
        targetValue = localPhase.color,
        animationSpec = tween(durationMillis = 650),
        label = "Local phase gradient",
    )
    val animatedRemoteColor by animateColorAsState(
        targetValue = remotePhase.color,
        animationSpec = tween(durationMillis = 650),
        label = "Remote phase gradient",
    )

    val onSurface = MaterialTheme.colorScheme.onSurface
    val background = MaterialTheme.colorScheme.background
    val shape = RoundedCornerShape(28.dp)

    val localStart = localDate.atStartOfDay(localZone)
    val localMiddle = localDate.atTime(12, 0).atZone(localZone)
    val localEnd = localDate.plusDays(1).atStartOfDay(localZone)
    val remoteStart = localStart.withZoneSameInstant(remoteZone)
    val remoteMiddle = localMiddle.withZoneSameInstant(remoteZone)
    val remoteEnd = localEnd.withZoneSameInstant(remoteZone)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            color = background,
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        animatedLocalColor.copy(alpha = 0.7f),
                        animatedRemoteColor.copy(alpha = 0.7f),
                    ),
                ),
            ),
            tonalElevation = 0.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithCache {
                        val topColors = Brush.horizontalGradient(
                            listOf(
                                animatedLocalColor.copy(alpha = 0.28f),
                                animatedRemoteColor.copy(alpha = 0.28f),
                            ),
                        )
                        val fade = Brush.verticalGradient(
                            0f to Color.Transparent,
                            1f to background,
                        )
                        onDrawBehind {
                            drawRect(color = background)
                            drawRect(brush = topColors)
                            drawRect(brush = fade)
                        }
                    },
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Text(
                        stringResource(R.string.compare_times),
                        style = MaterialTheme.typography.headlineSmall,
                        color = onSurface,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                stringResource(R.string.your_time),
                                style = MaterialTheme.typography.labelMedium,
                                color = onSurface.copy(alpha = 0.75f)
                            )
                            Text(
                                formatTime(local),
                                style = MaterialTheme.typography.headlineMedium,
                                color = onSurface
                            )
                            Text(
                                local.format(dateFormatter),
                                style = MaterialTheme.typography.labelSmall,
                                color = onSurface.copy(alpha = 0.75f)
                            )
                            Text(
                                "${localPhase.icon} ${stringResource(localPhase.labelRes)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = animatedLocalColor
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                timezone.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = onSurface.copy(alpha = 0.75f),
                                textAlign = TextAlign.End
                            )
                            Text(
                                formatTime(remote),
                                style = MaterialTheme.typography.headlineMedium,
                                color = onSurface
                            )
                            Text(
                                remote.format(dateFormatter),
                                style = MaterialTheme.typography.labelSmall,
                                color = onSurface.copy(alpha = 0.75f),
                                textAlign = TextAlign.End
                            )
                            Text(
                                "${remotePhase.icon} ${stringResource(remotePhase.labelRes)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = animatedRemoteColor,
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.your_time),
                            style = MaterialTheme.typography.labelMedium,
                            color = animatedLocalColor,
                        )
                        TimeLabels(
                            formatTime(localStart),
                            formatTime(localMiddle),
                            formatTime(localEnd),
                        )

                        val interactionSource = remember { MutableInteractionSource() }
                        val sliderColors = SliderDefaults.colors(thumbColor = onSurface)
                        val tickColors = (1 until 24).map { hour ->
                            val backgroundColor = if (minuteOfDay >= hour * 60) {
                                localPhase.color
                            } else {
                                remotePhase.color
                            }
                            val target = if (backgroundColor.luminance() > 0.40f) {
                                Color.Black
                            } else {
                                Color.White
                            }
                            val animated by animateColorAsState(
                                targetValue = target,
                                animationSpec = tween(durationMillis = 650),
                                label = "Hour $hour tick contrast",
                            )
                            animated
                        }
                        Slider(
                            value = minuteOfDay.toFloat(),
                            onValueChange = {
                                minuteOfDay = (it / 10f).roundToInt().coerceIn(0, 1440 / 10) * 10
                            },
                            valueRange = 0f..1440f,
                            steps = 0,
                            modifier = Modifier.fillMaxWidth(),
                            interactionSource = interactionSource,
                            thumb = {
                                SliderDefaults.Thumb(
                                    interactionSource = interactionSource,
                                    colors = sliderColors,
                                )
                            },
                            track = {
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(16.dp),
                                ) {
                                    val trackWidth = size.width
                                    val trackHeight = size.height
                                    val splitX = trackWidth * (minuteOfDay / 1440f)
                                    val radius = trackHeight / 2f
                                    val trackPath = Path().apply {
                                        addRoundRect(
                                            RoundRect(
                                                0f, 0f, trackWidth, trackHeight,
                                                CornerRadius(radius, radius),
                                            ),
                                        )
                                    }

                                    clipPath(trackPath) {
                                        // Solid, sharp color boundary at the slider position.
                                        drawRect(color = animatedRemoteColor)
                                        drawRect(
                                            color = animatedLocalColor,
                                            size = androidx.compose.ui.geometry.Size(
                                                splitX, trackHeight
                                            ),
                                        )
                                        for (hour in 1 until 24) {
                                            val x = trackWidth * hour / 24f
                                            val tickColor = tickColors[hour - 1]
                                            val isNoon = hour == 12
                                            val tickHeight =
                                                trackHeight * (if (isNoon) 0.88f else 0.54f)
                                            drawLine(
                                                color = tickColor.copy(alpha = if (isNoon) 1f else 0.88f),
                                                start = Offset(x, (trackHeight - tickHeight) / 2f),
                                                end = Offset(x, (trackHeight + tickHeight) / 2f),
                                                strokeWidth = (if (isNoon) 1.5.dp else 1.5.dp).toPx(),
                                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                            )
                                        }
                                    }
                                }
                            },
                        )

                        TimeLabels(
                            formatTime(remoteStart),
                            formatTime(remoteMiddle),
                            formatTime(remoteEnd),
                        )
                        Text(
                            timezone.displayName,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.labelMedium,
                            color = animatedRemoteColor,
                            textAlign = TextAlign.End,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = {
                                val now = ZonedDateTime.now(localZone)
                                minuteOfDay =
                                    ((now.hour * 60 + now.minute + 15) / 30 * 30).coerceIn(0, 1440)
                            },
                            border = BorderStroke(1.dp, animatedLocalColor.copy(alpha = 0.65f)),
                        ) { Text(stringResource(R.string.now)) }
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        ) { Text(stringResource(R.string.done)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeLabels(start: String, middle: String, end: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(start, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
        Text(
            middle,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
        Text(
            end,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.End
        )
    }
}