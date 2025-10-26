package com.example.test1.ui.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test1.ui.component.AppTopBar
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.test1.ui.schedule.components.DateHeader
import com.example.test1.ui.schedule.components.DaySchedule
import com.example.test1.ui.schedule.components.GroupSelector
import com.example.test1.ui.schedule.components.MonthCalendarDialog
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.example.test1.ui.schedule.components.ScrollableWeekRow
import java.time.temporal.ChronoUnit

data class ScheduleEvent(
    val title: String,
    val details: List<String>,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val color: Color
)

// Definicje stałych dla layoutu siatki
val HourHeight = 80.dp
const val DayStartHour = 7

private const val TOTAL_PAGES = 10400 // Wystarczy na ~200 lat (52 tygodnie * 200)
private const val STARTING_PAGE = TOTAL_PAGES / 2

/**
 * Oblicza indeks strony pagera dla podanej daty.
 * STARTING_PAGE (np. 5200) odpowiada tygodniowi `referenceDate`.
 */
private fun dateToPageIndex(date: LocalDate, referenceDate: LocalDate): Int {
    val dateWeekStart = date.minusDays(date.dayOfWeek.value.toLong() - 1)
    val refWeekStart = referenceDate.minusDays(referenceDate.dayOfWeek.value.toLong() - 1)
    val weeksBetween = ChronoUnit.WEEKS.between(refWeekStart, dateWeekStart)
    return (STARTING_PAGE + weeksBetween).toInt()
}

/**
 * Oblicza datę (poniedziałek) dla podanego indeksu strony pagera.
 */
private fun pageIndexToStartDate(page: Int, referenceDate: LocalDate): LocalDate {
    val refWeekStart = referenceDate.minusDays(referenceDate.dayOfWeek.value.toLong() - 1)
    val weekOffset = page - STARTING_PAGE
    return refWeekStart.plusWeeks(weekOffset.toLong())
}

@Composable
fun ScheduleScreen(
    onNavigateToSettings: () -> Unit,
    scheduleViewModel: ScheduleViewModel = hiltViewModel(),
) {
    val uiState by scheduleViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val tertiaryContainerColor = MaterialTheme.colorScheme.tertiaryContainer
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer

    // Stan specyficzny dla UI (np. widoczność dialogu) może pozostać lokalny
    var showMonthPicker by remember { mutableStateOf(false) }

    if (showMonthPicker) {
        MonthCalendarDialog(
            initialDate = uiState.selectedDate,
            onDateSelected = {
                scheduleViewModel.onDateSelected(it)
                showMonthPicker = false
            },
            onDismissRequest = { showMonthPicker = false }
        )
    }
    val referenceDate = remember { LocalDate.now() }

    val pagerState = rememberPagerState(
        initialPage = dateToPageIndex(uiState.selectedDate, referenceDate),
        pageCount = { TOTAL_PAGES }
    )
    LaunchedEffect(uiState.selectedDate) {
        val targetPage = dateToPageIndex(uiState.selectedDate, referenceDate)
        if (targetPage != pagerState.currentPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }
    LaunchedEffect(pagerState.isScrollInProgress) {
        // Reagujemy tylko po zakończeniu przewijania
        if (!pagerState.isScrollInProgress) {
            val newWeekStartDate = pageIndexToStartDate(pagerState.currentPage, referenceDate)

            // Zachowaj ten sam dzień tygodnia, który był wybrany, ale w nowym tygodniu
            val dayOfWeekOffset = uiState.selectedDate.dayOfWeek.value.toLong() - 1
            val newSelectedDate = newWeekStartDate.plusDays(dayOfWeekOffset)

            // Sprawdź, czy data faktycznie się zmieniła (żeby uniknąć pętli)
            if (newSelectedDate != uiState.selectedDate) {
                scheduleViewModel.onDateSelected(newSelectedDate)
            }
        }
    }

    // Stan dla wskaźnika aktualnego czasu może pozostać lokalny
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60000L)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage

        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            scheduleViewModel.onErrorShown()
        }
    }

    // Mapuj dane z Modelu (ScheduleItem) na dane dla Widoku (ScheduleEvent)
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { pageIndex ->
                    // Oblicz dni dla *tej konkretnej strony*
                    val pageStartDate = pageIndexToStartDate(pageIndex, referenceDate)
                    val weekDaysForPage = List(7) { i -> pageStartDate.plusDays(i.toLong()) }

                    ScrollableWeekRow(
                        dates = weekDaysForPage,
                        selectedDate = uiState.selectedDate,
                        onDateSelected = { newDate -> scheduleViewModel.onDateSelected(newDate) }
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Box(modifier = Modifier.fillMaxSize()) {
                    if (!uiState.isLoading) {
                        if (displayEvents.isNotEmpty()) {
                            DaySchedule(
                                events = displayEvents,
                                isToday = uiState.selectedDate == LocalDate.now(),
                                currentTime = currentTime
                            )
                        } else {
                            if (uiState.errorMessage == null) {
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
                            }
                        }
                    }
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}














