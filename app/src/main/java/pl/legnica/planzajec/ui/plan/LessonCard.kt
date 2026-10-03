package pl.legnica.planzajec.ui.plan

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.legnica.planzajec.data.local.entity.LessonEntity
import pl.legnica.planzajec.parser.model.ChangeFlag
import pl.legnica.planzajec.ui.theme.ChangeColorCancelled
import pl.legnica.planzajec.ui.theme.ChangeColorModified
import pl.legnica.planzajec.ui.theme.ChangeColorNew
import pl.legnica.planzajec.ui.theme.DarkSurface
import pl.legnica.planzajec.ui.theme.DarkSurfaceBorder
import pl.legnica.planzajec.ui.theme.DarkSurfaceElevated
import pl.legnica.planzajec.ui.theme.LessonGradients
import pl.legnica.planzajec.ui.theme.TextMuted
import pl.legnica.planzajec.ui.theme.TextPrimary
import pl.legnica.planzajec.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonCard(
    lesson: LessonEntity,
    onTeacherClick: ((String) -> Unit)? = null,
    onRoomClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val now = LocalTime.now()
    val today = LocalDate.now()
    val isToday = lesson.date == today
    val isOngoing = isToday && !now.isBefore(lesson.startTime) && now.isBefore(lesson.endTime)
    val isFinished = isToday && now.isAfter(lesson.endTime)

    // Ongoing pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "cardPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val cardBorder = when {
        isOngoing -> BorderStrokeGradient(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF00D2FF), Color(0xFFEC4899), Color(0xFF8B5CF6))))
        lesson.changeFlag == ChangeFlag.CHANGED_ROOM_OR_TIME -> BorderStrokeGradient(1.dp, Brush.horizontalGradient(listOf(ChangeColorModified, ChangeColorModified)))
        lesson.changeFlag == ChangeFlag.NEW -> BorderStrokeGradient(1.dp, Brush.horizontalGradient(listOf(ChangeColorNew, ChangeColorNew)))
        lesson.changeFlag == ChangeFlag.CANCELLED -> BorderStrokeGradient(1.dp, Brush.horizontalGradient(listOf(ChangeColorCancelled, ChangeColorCancelled)))
        else -> BorderStrokeGradient(1.dp, pl.legnica.planzajec.ui.theme.GlassTokens.BorderBrush)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF0F172A).copy(alpha = if (isOngoing) 0.75f else 0.55f))
            .border(cardBorder.width, cardBorder.brush, RoundedCornerShape(18.dp))
            .alpha(if (isFinished) 0.6f else 1.0f)
    ) {
        // Specular top reflection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(pl.legnica.planzajec.ui.theme.GlassTokens.SpecularHighlight)
        )

        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Left Column: Times
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(62.dp)
                ) {
                    Text(
                        text = lesson.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isOngoing) Color(0xFF00D2FF) else TextPrimary
                    )
                    Text(
                        text = lesson.endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    if (isOngoing) {
                        Text(
                            text = "TERAZ",
                            color = Color(0xFF00D2FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .alpha(pulseAlpha)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right Column: Details
                Column(modifier = Modifier.weight(1f)) {
                    // Subject Name
                    Text(
                        text = lesson.subjectFull.ifBlank { lesson.subjectShort },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Chips Row
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Lesson Type Chip
                        LessonTypeChip(type = lesson.type, rawType = lesson.rawType)

                        // Room / Online Chip
                        if (lesson.isOnline) {
                            OnlineRoomChip(onlineId = lesson.onlineId)
                        } else {
                            RoomChip(
                                room = lesson.room,
                                onClick = { onRoomClick?.invoke(lesson.room) }
                            )
                        }

                        // Change Flag Chip (if modified/new/cancelled)
                        if (lesson.changeFlag != ChangeFlag.NONE) {
                            ChangeBadge(flag = lesson.changeFlag, note = lesson.previousRoomOrTimeNote)
                        }
                    }

                    // Teacher
                    if (lesson.teacher.isNotBlank() && lesson.teacher != "-") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onTeacherClick?.invoke(lesson.teacher) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = lesson.teacher,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Ongoing Progress Bar
            if (isOngoing) {
                Spacer(modifier = Modifier.height(10.dp))
                val totalSeconds = (lesson.endTime.toSecondOfDay() - lesson.startTime.toSecondOfDay()).toFloat()
                val elapsedSeconds = (now.toSecondOfDay() - lesson.startTime.toSecondOfDay()).coerceIn(0, totalSeconds.toInt()).toFloat()
                val progress = if (totalSeconds > 0) elapsedSeconds / totalSeconds else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF00D2FF),
                    trackColor = DarkSurfaceBorder
                )
            }
        }
    }
}

@Composable
private fun LessonTypeChip(type: pl.legnica.planzajec.parser.model.LessonType, rawType: String) {
    val gradient = LessonGradients.getBrushForType(type)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, gradient, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (type == pl.legnica.planzajec.parser.model.LessonType.OTHER && rawType.isNotBlank()) rawType else type.displayName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun OnlineRoomChip(onlineId: String?) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(LessonGradients.OnlineBrush)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = "Online",
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (onlineId != null) "ONLINE $onlineId" else "ONLINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun RoomChip(room: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.MeetingRoom,
                contentDescription = "Sala",
                tint = TextSecondary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = room,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun ChangeBadge(flag: ChangeFlag, note: String?) {
    val (bgColor, label) = when (flag) {
        ChangeFlag.NEW -> Pair(ChangeColorNew, "Nowe zajęcia")
        ChangeFlag.CHANGED_ROOM_OR_TIME -> Pair(ChangeColorModified, note ?: "Zmieniono")
        ChangeFlag.CANCELLED -> Pair(ChangeColorCancelled, "Odwołane")
        ChangeFlag.NONE -> return
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor.copy(alpha = 0.2f))
            .border(1.dp, bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = bgColor
        )
    }
}

private data class BorderStrokeGradient(val width: androidx.compose.ui.unit.Dp, val brush: Brush)
