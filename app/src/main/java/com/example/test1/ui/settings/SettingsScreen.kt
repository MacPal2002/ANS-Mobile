package com.example.test1.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test1.ui.component.AppTopBar
import androidx.lifecycle.viewmodel.compose.viewModel


val destructiveColor = Color(0xFFD32F2F)/**/

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToGroupSelection: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showTimeDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val timeOptions = listOf("15 minut", "30 minut", "1 godzina", "2 godziny")
    val themeOptions = listOf("Jasny", "Ciemny", "Systemowy")

    // Obsługa błędów
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    if (showTimeDialog) {
        SettingsOptionPickerDialog(
            title = "Wybierz wyprzedzenie",
            options = timeOptions,
            selectedOption = uiState.notificationTimeOption,
            onOptionSelected = {
                viewModel.onNotificationTimeChange(it)
                showTimeDialog = false
            },
            onDismissRequest = { showTimeDialog = false }
        )
    }

    if (showThemeDialog) {
        SettingsOptionPickerDialog(
            title = "Wybierz motyw aplikacji",
            options = themeOptions,
            selectedOption = uiState.themeOption,
            onOptionSelected = {
                viewModel.onThemeChange(it)
                showThemeDialog = false
            },
            onDismissRequest = { showThemeDialog = false }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationIconClick = onNavigateBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    ProfileHeader(
                        fullName = uiState.displayName,
                        albumNumber = uiState.albumNumber,
                        deanGroups = uiState.observedGroups,
                        onNavigateToGroupSelection = onNavigateToGroupSelection
                    )
                }

                item {
                    Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                        SettingsSection {
                            SectionHeader(title = "PERSONALIZACJA")
                            SettingsClickableRow(
                                title = "Motyw aplikacji",
                                value = uiState.themeOption,
                                onClick = { showThemeDialog = true }
                            )
                        }
                    }
                }
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SettingsSection {
                            SectionHeader(title = "POWIADOMIENIA O ZAJĘCIACH")
                            SettingsToggleRow(
                                title = "Wysyłaj powiadomienia",
                                checked = uiState.notificationsEnabled,
                                onCheckedChange = viewModel::onNotificationsToggle
                            )
                            SettingsClickableRow(
                                title = "Wyprzedzenie",
                                value = uiState.notificationTimeOption,
                                onClick = { showTimeDialog = true }
                            )
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        CreativeAppInfoCard()
                    }
                }

                item {
                    TextButton(
                        onClick = {
                            viewModel.onLogout()
                            onLogout()
                        },
                        modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
                    ) {
                        Text(
                            text = "Wyloguj się",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}



