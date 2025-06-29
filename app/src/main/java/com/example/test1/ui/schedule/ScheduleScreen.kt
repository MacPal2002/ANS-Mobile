package com.example.test1.ui.schedule

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.test1.ui.component.AppTopBar
import com.example.test1.ui.login.primaryColor
import com.example.test1.ui.login.subtextColor
import com.example.test1.ui.settings.secondaryColor
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// --- DEFINICJE STYLU I DANYCH ---


val eventColor1 = Color(0xFFE8EAF6)
val eventColor2 = Color(0xFFDCEEEB)
val eventColor3 = Color(0xFFF8EAEA)
val textColor = Color(0xFF1F1F1F)


data class ScheduleEvent(
    val title: String,
    val details: List<String>,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val color: Color
)

// Nowa, rozbudowana lista wydarzeń
val allMockEvents = listOf(
    // --- WCZORAJ ---
    ScheduleEvent("Algebra liniowa", listOf("prof. dr hab. Ewa Czerwińska", "A-1 404", "wykład"), LocalDateTime.now().minusDays(1).withHour(11).withMinute(15), LocalDateTime.now().minusDays(1).withHour(13).withMinute(0), eventColor1),
    ScheduleEvent("Wychowanie fizyczne", listOf("mgr Janusz Sportowy", "Hala sportowa"), LocalDateTime.now().minusDays(1).withHour(15).withMinute(0), LocalDateTime.now().minusDays(1).withHour(16).withMinute(30), eventColor3),

    // --- DZISIAJ ---
    ScheduleEvent("Język włoski", listOf("mgr Agata Borkowska", "Bud.gł. 3", "lektorat"), LocalDateTime.now().withHour(8).withMinute(0), LocalDateTime.now().withHour(9).withMinute(30), eventColor1),
    ScheduleEvent("Przerwa obiadowa", listOf("Czas na odpoczynek"), LocalDateTime.now().withHour(9).withMinute(30), LocalDateTime.now().withHour(11).withMinute(30), eventColor3),
    ScheduleEvent("Metody behawioralne", listOf("dr Magdalena Adamczyk-Kowalczuk", "Paw.F 615", "ćwiczenia"), LocalDateTime.now().withHour(11).withMinute(30), LocalDateTime.now().withHour(13).withMinute(0), eventColor2),
    ScheduleEvent("Inżynieria oprogramowania", listOf("dr inż. Adam Nowak", "Lab. 201, C-3", "laboratorium"), LocalDateTime.now().withHour(13).withMinute(15), LocalDateTime.now().withHour(15).withMinute(0), eventColor1),

    // --- JUTRO ---
    ScheduleEvent("Analiza matematyczna", listOf("prof. dr hab. Jan Nowak", "A-2 301", "wykład"), LocalDateTime.now().plusDays(1).withHour(10).withMinute(0), LocalDateTime.now().plusDays(1).withHour(11).withMinute(30), eventColor1),
    ScheduleEvent("Bazy danych", listOf("dr inż. Anna Kulesza", "Lab. 108, C-2", "laboratorium"), LocalDateTime.now().plusDays(1).withHour(11).withMinute(45), LocalDateTime.now().plusDays(1).withHour(13).withMinute(15), eventColor2),

    // --- POJUTRZE (brak zajęć, aby przetestować pusty ekran) ---

    // --- ZA 3 DNI ---
    ScheduleEvent("Programowanie mobilne", listOf("mgr inż. Paweł Kowalski", "Lab. 315, C-1", "projekt"), LocalDateTime.now().plusDays(3).withHour(9).withMinute(0), LocalDateTime.now().plusDays(3).withHour(11).withMinute(0), eventColor2)
)

// Definicje stałych dla layoutu siatki
private val HourHeight = 80.dp
private val DayStartHour = 7

// --- GŁÓWNY EKRAN PLANU ZAJĘĆ ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onNavigateToSettings: () -> Unit,
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showMonthPicker by remember { mutableStateOf(false) }

    if (showMonthPicker) {
        MonthCalendarDialog(
            initialDate = selectedDate,
            onDateSelected = {
                selectedDate = it
                showMonthPicker = false
            },
            onDismissRequest = { showMonthPicker = false }
        )
    }

    val weekDays = remember(selectedDate) {
        val startOfWeek = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)
        List(7) { i -> startOfWeek.plusDays(i.toLong()) }
    }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60000L)
        }
    }
    val filteredEvents = remember(selectedDate, allMockEvents) {
        allMockEvents.filter { event -> event.startTime.toLocalDate() == selectedDate }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                AppTopBar(
                    actionIcon = Icons.Default.Settings,
                    onActionClick = { onNavigateToSettings() },
                )
            },
            containerColor = Color.White
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                DateHeader(
                    selectedDate = selectedDate,
                    onTodayClick = { selectedDate = LocalDate.now() },
                    onDateClick = { showMonthPicker = true }
                )
                HorizontalDayPicker(
                    dates = weekDays,
                    selectedDate = selectedDate,
                    onDateSelected = { newDate -> selectedDate = newDate }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                DaySchedule(
                    events = filteredEvents,
                    isToday = selectedDate == LocalDate.now(),
                    currentTime = currentTime
                )
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

    // Automatyczne przewinięcie do aktualnej godziny przy starcie
    LaunchedEffect(Unit) {
        val initialOffset = (currentTime.hour - DayStartHour - 1) * HourHeight.value
        scrollState.scrollTo(initialOffset.toInt().coerceAtLeast(0))
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
                    color = subtextColor
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

    // ZMIANA: Dodajemy Box jako główny kontener, aby móc rysować tło i layout
    Box(modifier = modifier) {

        // ZMIANA: Dodajemy Canvas do rysowania linii godzinowych w tle.
        // Te linie będą się przewijać razem z resztą.
        Canvas(modifier = Modifier.matchParentSize()) {
            for (hour in DayStartHour..22) {
                val yPosition = (hour - DayStartHour) * hourHeightPx
                drawLine(
                    color = Color(0xFFE0E0E0), // Możesz tu zmienić kolor/przezroczystość linii
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
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                event.details.forEach { detail ->
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = subtextColor
                    )
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
                    tint = subtextColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = timeRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtextColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
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
        "EEEE, d MMMM" // Format bez roku, np. "Wtorek, 24 czerwca"
    } else {
        "EEEE, d MMMM uuuu" // Pełny format z rokiem, np. "Środa, 25 czerwca 2026"
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
        // Pusty Box po lewej stronie dla zachowania symetrii
        Box(
            modifier = Modifier.width(90.dp),
            contentAlignment = Alignment.Center
        ){
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(secondaryColor)
                    .clickable(onClick = onDateClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = "Otwórz kalendarz",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Tytuł z datą
        Text(
            text = selectedDate.format(formatter).replaceFirstChar { it.titlecase(Locale("pl")) },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = primaryColor,
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
                        .background(secondaryColor)
                        .clickable(onClick = onTodayClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Wróć do dzisiaj",
                        tint = primaryColor,
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
        contentPadding = PaddingValues(horizontal = 16.dp),
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
    val dayOfWeek = date.format(dayOfWeekFormatter).uppercase()

    val containerColor = if (isSelected) primaryColor else Color.Transparent
    val contentColor = if (isSelected) Color.White else textColor

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
        Text(text = dayOfWeek, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
        Text(text = dayOfMonth, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = contentColor)
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
                            Text(text = day, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = subtextColor)
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
        isSelected -> primaryColor
        isToday -> secondaryColor
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> Color.White
        isToday -> primaryColor
        else -> textColor
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