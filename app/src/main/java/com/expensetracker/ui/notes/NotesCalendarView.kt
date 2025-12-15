package com.expensetracker.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.expensetracker.data.models.Note
import com.expensetracker.ui.components.LiquidGlassCard
import com.expensetracker.ui.utils.AppColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesCalendarView(
    notes: List<Note>,
    selectedDate: Date?,
    onDateSelected: (Date) -> Unit,
    onNoteTap: (Note) -> Unit,
    modifier: Modifier = Modifier,
    isLight: Boolean = MaterialTheme.colorScheme.surface == Color.White
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance().apply { time = Date() }) }
    val calendar = Calendar.getInstance()
    
    // Даты с заметками
    val datesWithNotes = remember(notes) {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        notes.mapNotNull { note ->
            if (note.noteDate != null) {
                formatter.format(Date(note.noteDate))
            } else null
        }.toSet()
    }
    
    // Заметки на выбранную дату
    val notesForSelectedDate = remember(notes, selectedDate) {
        if (selectedDate == null) return@remember emptyList<Note>()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateString = formatter.format(selectedDate)
        notes.filter { note ->
            note.noteDate != null && formatter.format(Date(note.noteDate)) == selectedDateString
        }
    }
    
    Column(modifier = modifier) {
        // Заголовок с навигацией
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                currentMonth.add(Calendar.MONTH, -1)
            }) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Предыдущий месяц",
                    tint = AppColors.primaryText(isLight)
                )
            }
            
            Text(
                text = SimpleDateFormat("MMMM yyyy", Locale("ru")).format(currentMonth.time),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.primaryText(isLight)
            )
            
            IconButton(onClick = {
                currentMonth.add(Calendar.MONTH, 1)
            }) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Следующий месяц",
                    tint = AppColors.primaryText(isLight)
                )
            }
        }
        
        // Календарная сетка
        CalendarGrid(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            datesWithNotes = datesWithNotes,
            onDateClick = onDateSelected,
            isLight = isLight
        )
        
        // Заметки на выбранную дату
        if (selectedDate != null && notesForSelectedDate.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Заметки на ${SimpleDateFormat("dd MMMM", Locale("ru")).format(selectedDate)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.primaryText(isLight),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Важно: здесь НЕ используем LazyColumn, чтобы не вкладывать её в внешнюю LazyColumn NotesScreen
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                notesForSelectedDate.forEach { note ->
                    NoteCard(
                        note = note,
                        onClick = { onNoteTap(note) },
                        isLight = isLight
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    selectedDate: Date?,
    datesWithNotes: Set<String>,
    onDateClick: (Date) -> Unit,
    isLight: Boolean
) {
    val calendar = Calendar.getInstance()
    calendar.time = currentMonth.time
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // Начинаем с понедельника (в Java Calendar воскресенье = 1, понедельник = 2)
    val startOffset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
    
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Дни недели
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.secondaryText(isLight),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Дни месяца
        var dayIndex = 0
        while (dayIndex < startOffset + daysInMonth) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { weekDay ->
                    if (dayIndex < startOffset) {
                        // Пустые ячейки в начале
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val day = dayIndex - startOffset + 1
                        if (day <= daysInMonth) {
                            val dayCalendar = Calendar.getInstance()
                            dayCalendar.time = currentMonth.time
                            dayCalendar.set(Calendar.DAY_OF_MONTH, day)
                            val dayDate = dayCalendar.time
                            val dateString = formatter.format(dayDate)
                            
                            val isToday = Calendar.getInstance().let {
                                val today = it.time
                                it.time = dayDate
                                val isSameDay = it.get(Calendar.YEAR) == Calendar.getInstance().apply { time = today }.get(Calendar.YEAR) &&
                                        it.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().apply { time = today }.get(Calendar.DAY_OF_YEAR)
                                isSameDay
                            }
                            
                            val isSelected = selectedDate != null && Calendar.getInstance().let {
                                it.time = selectedDate
                                val selectedYear = it.get(Calendar.YEAR)
                                val selectedDay = it.get(Calendar.DAY_OF_YEAR)
                                it.time = dayDate
                                it.get(Calendar.YEAR) == selectedYear && it.get(Calendar.DAY_OF_YEAR) == selectedDay
                            }
                            
                            val hasNotes = datesWithNotes.contains(dateString)
                            
                            DayCell(
                                day = day,
                                isToday = isToday,
                                isSelected = isSelected,
                                hasNotes = hasNotes,
                                onClick = { onDateClick(dayDate) },
                                isLight = isLight,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                    dayIndex++
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    hasNotes: Boolean,
    onClick: () -> Unit,
    isLight: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$day",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> Color.White
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> AppColors.primaryText(isLight)
                }
            )
            if (hasNotes) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    isLight: Boolean
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        LiquidGlassCard(isLight = isLight) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.primaryText(isLight)
                    )
                    if (note.reminderDate != null) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFFFF9500),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                val contentText = note.content
                if (contentText != null && contentText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = contentText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.secondaryText(isLight),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

