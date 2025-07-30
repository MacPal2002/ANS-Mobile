package com.example.test1.ui.schedule

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Group
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test1.ui.component.AppTopBar
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import com.example.test1.data.ObservedGroup

data class ScheduleEvent(
    val title: String,
    val details: List<String>,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val color: Color
)

// Definicje stałych dla layoutu siatki
private val HourHeight = 80.dp
private val DayStartHour = 7

// --- GŁÓWNY EKRAN PLANU ZAJĘĆ ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onNavigateToSettings: () -> Unit,
    scheduleViewModel: ScheduleViewModel = viewModel(), // Wstrzyknięcie ViewModel
) {
    // Krok 1: Obserwuj stan z ViewModelu
    val uiState by scheduleViewModel.uiState.collectAsState()

    val tertiaryContainerColor = MaterialTheme.colorScheme.tertiaryContainer
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer

    // Stan specyficzny dla UI (np. widoczność dialogu) może pozostać lokalny
    var showMonthPicker by remember { mutableStateOf(false) }

    if (showMonthPicker) {
        MonthCalendarDialog(
            initialDate = uiState.selectedDate,
            onDateSelected = {
                // Krok 2: Akcje użytkownika wywołują metody na ViewModelu
                scheduleViewModel.onDateSelected(it)
                showMonthPicker = false
            },
            onDismissRequest = { showMonthPicker = false }
        )
    }

    // Obliczenia zależne od stanu wykonujemy na danych z ViewModelu
    val weekDays = remember(uiState.selectedDate) {
        val startOfWeek = uiState.selectedDate.minusDays(uiState.selectedDate.dayOfWeek.value.toLong() - 1)
        List(7) { i -> startOfWeek.plusDays(i.toLong()) }
    }

    // Stan dla wskaźnika aktualnego czasu może pozostać lokalny
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60000L)
        }
    }

    // Krok 3: Mapuj dane z Modelu (ScheduleItem) na dane dla Widoku (ScheduleEvent)
    val displayEvents = remember(uiState.events) {
        uiState.events.map { item ->
            ScheduleEvent(
                title = item.subjectFullName,
                details = listOfNotNull(
                    item.lecturers.firstOrNull()?.get("name") as? String,
                    item.rooms.firstOrNull()?.get("name") as? String,
                    item.classType?.let { "Rodzaj: $it" }
                ),
                startTime = item.getStartLocalDateTime(),
                endTime = item.getEndLocalDateTime(),
                color = if ((item.classType ?: "").contains("W", ignoreCase = true)) {
                    tertiaryContainerColor
                } else {
                    secondaryContainerColor
                }
            )
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                AppTopBar(
                    actionIcon = Icons.Default.Settings,
                    onActionClick = onNavigateToSettings,
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                DateHeader(
                    selectedDate = uiState.selectedDate,
                    onTodayClick = { scheduleViewModel.onDateSelected(LocalDate.now()) },
                    onDateClick = { showMonthPicker = true }
                )
                if (uiState.observedGroups.size > 1) {
                    GroupSelector(
                        groups = uiState.observedGroups,
                        selectedGroupId = uiState.selectedGroupId,
                        onGroupSelected = { scheduleViewModel.onGroupSelected(it) }
                    )
                }
                HorizontalDayPicker(
                    dates = weekDays,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { newDate -> scheduleViewModel.onDateSelected(newDate) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Krok 4: Wyświetlaj UI w zależności od stanu (ładowanie, błąd, sukces)
                Box(modifier = Modifier.fillMaxSize()) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center).padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    } else if (displayEvents.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Brak zajęć",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Brak zajęć",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        DaySchedule(
                            events = displayEvents,
                            isToday = uiState.selectedDate == LocalDate.now(),
                            currentTime = currentTime
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSelector(
    groups: List<ObservedGroup>,
    selectedGroupId: Int?,
    onGroupSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedGroupName = groups.find { it.id == selectedGroupId }?.name ?: "Wybierz grupę"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedGroupName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Wybrana grupa", style = MaterialTheme.typography.labelLarge) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Grupa"
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.LightGray,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                )
            )

            // ✅ ZMIANY W ROZWIJANYM MENU
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.name) },
                        onClick = {
                            onGroupSelected(group.id)
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

// --- GŁÓWNY KOMPONENT NOWEGO WIDOKU PLANU ---

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DaySchedule(
    events: List<ScheduleEvent>,
    isToday: Boolean,
    currentTime: LocalTime,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val density = LocalDensity.current
    val hourHeightPx = remember(density) {
        with(density) { HourHeight.toPx() }
    }

    // Efekt będzie uruchamiany za każdym razem, gdy zmieni się lista wydarzeń (czyli dzień)
    LaunchedEffect(events) {
        if (isToday) {
            // Dla dzisiejszego dnia: przewiń do aktualnej godziny
            val hourOffset = currentTime.hour - DayStartHour
            val minuteOffset = currentTime.minute / 60f
            val totalOffsetPx = (hourOffset + minuteOffset) * hourHeightPx
            scrollState.animateScrollTo(totalOffsetPx.toInt().coerceAtLeast(0))
        } else {
            // ✅ NOWA LOGIKA: Dla innych dni
            if (events.isNotEmpty()) {
                // Jeśli są zajęcia, znajdź pierwsze z nich
                val firstEvent = events.minByOrNull { it.startTime }
                if (firstEvent != null) {
                    // Oblicz pozycję pierwszych zajęć i przewiń do nich
                    val hourOffset = firstEvent.startTime.hour - DayStartHour
                    val minuteOffset = firstEvent.startTime.minute / 60f
                    val totalOffsetPx = (hourOffset + minuteOffset) * hourHeightPx
                    // Przewijamy odrobinę wyżej, aby był margines
                    val finalOffset = (totalOffsetPx - (hourHeightPx / 2)).coerceAtLeast(0f)
                    scrollState.animateScrollTo(finalOffset.toInt())
                }
            } else {
                // Jeśli nie ma zajęć, przewiń na samą górę
                scrollState.animateScrollTo(0)
            }
        }
    }

    // Tło z liniami godzinowymi
    Row(
        modifier = modifier
            .fillMaxSize() // Dodajemy fillMaxSize, aby zająć dostępną przestrzeň
            .verticalScroll(scrollState)
            .padding(top = 30.dp)
    ) {
        // Kolumna z godzinami po lewej
        TimeGutter(hourHeight = HourHeight)

        // Siatka z wydarzeniami, która teraz sama rysuje swoje tło
        EventGrid(
            events = events,
            isToday = isToday,
            currentTime = currentTime,
            hourHeight = HourHeight,
            modifier = Modifier.weight(1f)
        )
    }
}

// --- KOMPONENTY POMOCNICZE DLA SIATKI GODZINOWEJ ---

@SuppressLint("DefaultLocale")
@Composable
fun TimeGutter(hourHeight: Dp, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        for (hour in DayStartHour..22) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(hourHeight),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = String.format("%d:00", hour),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EventGrid(
    events: List<ScheduleEvent>,
    isToday: Boolean,
    currentTime: LocalTime,
    hourHeight: Dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val hourHeightPx = with(density) { hourHeight.toPx() }

    val lineColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
    // ZMIANA: Dodajemy Box jako główny kontener, aby móc rysować tło i layout
    Box(modifier = modifier) {

        // ZMIANA: Dodajemy Canvas do rysowania linii godzinowych w tle.
        // Te linie będą się przewijać razem z resztą.
        Canvas(modifier = Modifier.matchParentSize()) {
            for (hour in DayStartHour..22) {
                val yPosition = (hour - DayStartHour) * hourHeightPx
                drawLine(
                    color = lineColor,
                    start = Offset(x = 0f, y = yPosition),
                    end = Offset(x = size.width, y = yPosition),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Twój kod z Layout pozostaje w środku, jest teraz rysowany na wierzchu linii
        Layout(
            content = {
                events.forEach { event ->
                    Box(modifier = Modifier.eventData(event)) {
                        EventCard(event)
                    }
                }
                if (isToday) {
                    CurrentTimeLine()
                }
            },
        ) { measurables, constraints ->
            val placeablesWithData = measurables.map { measurable ->
                val eventData = measurable.parentData as? EventData
                val placeable = if (eventData != null) {
                    val event = eventData.event
                    val startOffsetPx = (event.startTime.hour - DayStartHour) * hourHeightPx + (event.startTime.minute / 60f * hourHeightPx)
                    val endOffsetPx = (event.endTime.hour - DayStartHour) * hourHeightPx + (event.endTime.minute / 60f * hourHeightPx)
                    val heightPx = (endOffsetPx - startOffsetPx).coerceAtLeast(0f).toInt()
                    measurable.measure(
                        constraints.copy(minHeight = heightPx, maxHeight = heightPx)
                    )
                } else {
                    measurable.measure(constraints)
                }
                Pair(placeable, eventData)
            }

            layout(constraints.maxWidth, (hourHeightPx * (23 - DayStartHour)).toInt()) {
                placeablesWithData.forEach { (placeable, eventData) ->
                    if (eventData != null) {
                        val event = eventData.event
                        val startOffsetPx = (event.startTime.hour - DayStartHour) * hourHeightPx + (event.startTime.minute / 60f * hourHeightPx)
                        placeable.place(x = 0, y = startOffsetPx.toInt())
                    } else {
                        val offsetPx = (currentTime.hour - DayStartHour) * hourHeightPx + (currentTime.minute / 60f * hourHeightPx)
                        placeable.place(x = 0, y = offsetPx.toInt())
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: ScheduleEvent) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val timeRange = "${event.startTime.toLocalTime().format(timeFormatter)} - ${event.endTime.toLocalTime().format(timeFormatter)}"

    Card(
        modifier = Modifier
            .fillMaxSize()
            // ZMIANA 3: Dodajemy padding na dole, aby stworzyć odstęp
            .padding(end = 16.dp, start = 8.dp, bottom = 8.dp),
        shape = RoundedCornerShape(12.dp), // Ładniejsze, bardziej zaokrąglone rogi
        colors = CardDefaults.cardColors(containerColor = event.color)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            // Główna treść, wyrównana do góry
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = event.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                event.details.forEach { detail ->
                    Text(text = detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            // Godzina, "przyklejona" do prawego dolnego rogu
            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Czas trwania",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = timeRange, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun CurrentTimeLine() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
        Divider(color = Color.Red, thickness = 2.dp)
    }
}

// Customowy modifier do przekazywania danych do layoutu
private class EventData(val event: ScheduleEvent) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@EventData
}
private fun Modifier.eventData(event: ScheduleEvent) = this.then(EventData(event))


// --- POZOSTAŁE KOMPONENTY POMOCNICZE ---

@Composable
fun DateHeader(
    selectedDate: LocalDate,
    onTodayClick: () -> Unit,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val currentYear = LocalDate.now().year
    val pattern = if (selectedDate.year == currentYear) {
        "E, d MMMM" // Format bez roku, np. "Wt., 24 czerwca"
    } else {
        "E, d MMMM uuuu" // Pełny format z rokiem, np. "Śr., 25 czerwca 2026"
    }
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale("pl"))
    val isToday = selectedDate == LocalDate.now()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(90.dp),
            contentAlignment = Alignment.Center
        ){
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .clickable(onClick = onDateClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = "Otwórz kalendarz",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Tytuł z datą
        Text(
            text = selectedDate.format(formatter).replaceFirstChar { it.titlecase(Locale("pl")) },
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // Kontener na przycisk po prawej stronie
        Box(
            modifier = Modifier.width(90.dp),
            contentAlignment = Alignment.Center
        ) {

            if (!isToday) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .clickable(onClick = onTodayClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Wróć do dzisiaj",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalDayPicker(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dates) { date ->
            DayPill(
                date = date,
                isSelected = date == selectedDate,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
fun DayPill(date: LocalDate, isSelected: Boolean, onClick: () -> Unit) {
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E", Locale("pl"))
    val dayOfMonth = date.dayOfMonth.toString()
    val dayOfWeekText = date.format(dayOfWeekFormatter).uppercase()

    val isToday = date == LocalDate.now()
    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

    // --- NOWA, ZUNIFIKOWANA LOGIKA KOLORÓW ---

    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primary     // Ciemne tło dla zaznaczonego dnia
        isToday -> MaterialTheme.colorScheme.tertiaryContainer      // Jasne tło dla dzisiejszego dnia
        else -> Color.Transparent      // Brak tła dla pozostałych
    }

    val dayOfMonthColor = when {
        isSelected -> Color.White      // Biały tekst na ciemnym tle
        isToday -> MaterialTheme.colorScheme.onTertiaryContainer       // Ciemny tekst (główny kolor) na jasnym tle
        else -> MaterialTheme.colorScheme.onBackground              // Domyślny kolor tekstu
    }

    val dayOfWeekColor = when {
        isSelected -> Color.White
        isToday -> MaterialTheme.colorScheme.onTertiaryContainer
        isWeekend -> Color(0xFFB71C1C)  // Czerwony dla weekendu
        else -> MaterialTheme.colorScheme.onBackground
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .width(56.dp)
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = dayOfWeekText, style = MaterialTheme.typography.labelSmall, color = dayOfWeekColor)
        Text(text = date.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, color = dayOfMonthColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthCalendarDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    val daysOfWeek = listOf("PN", "WT", "ŚR", "CZW", "PT", "SO", "ND")

    AlertDialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.width(320.dp).height(440.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Nagłówek z nazwą miesiąca i nawigacją
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) {
                        Icon(Icons.Default.ChevronLeft, "Poprzedni miesiąc")
                    }
                    Text(
                        text = "${displayedMonth.month.getDisplayName(TextStyle.FULL, Locale("pl")).replaceFirstChar { it.titlecase() }} ${displayedMonth.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) {
                        Icon(Icons.Default.ChevronRight, "Następny miesiąc")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Nagłówki dni tygodnia
                Row(modifier = Modifier.fillMaxWidth()) {
                    daysOfWeek.forEach { day ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(text = day, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Siatka z dniami
                CalendarGrid(
                    displayedMonth = displayedMonth,
                    selectedDate = initialDate,
                    onDateSelected = onDateSelected
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = displayedMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 for Monday, 7 for Sunday
    val daysInMonth = displayedMonth.lengthOfMonth()

    // Obliczamy "puste" komórki na początku siatki
    val emptyCells = firstDayOfWeek - 1

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Tworzymy listę wszystkich 42 komórek siatki (6 wierszy * 7 dni)
        val allCells = (1..emptyCells).map { null } + (1..daysInMonth).map { displayedMonth.atDay(it) }
        val chunks = allCells.chunked(7) // Dzielimy na tygodnie

        chunks.forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (date != null) {
                            CalendarDay(
                                date = date,
                                isSelected = date == selectedDate,
                                onDateSelected = { onDateSelected(it) }
                            )
                        }
                    }
                }
                // Jeśli tydzień jest niepełny, dodajemy puste Boxy dla zachowania układu
                if (week.size < 7) {
                    for (i in 1..(7 - week.size)) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val isToday = date == LocalDate.now()
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.secondary
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> Color.White
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f) // Zapewnia kwadratowy kształt
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onDateSelected(date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = textColor,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}