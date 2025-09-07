package com.example.test1.ui.settings


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.test1.ui.component.AppTopBar
import com.example.test1.ui.component.SectionHeader
import com.example.test1.util.checkNotificationPermission
import com.example.test1.ui.settings.components.SettingsSection
import com.example.test1.ui.settings.components.SettingsClickableRow
import com.example.test1.ui.settings.components.SettingsOptionPickerDialog
import com.example.test1.ui.settings.components.SettingsToggleRow
import com.example.test1.ui.settings.components.ProfileHeader
import com.example.test1.ui.settings.components.CreativeAppInfoCard


val destructiveColor = Color(0xFFD32F2F)/**/
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToGroupSelection: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by settingsViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showTimeDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val timeOptions = listOf("15 minut", "30 minut", "1 godzina", "2 godziny")
    val themeOptions = listOf("Jasny", "Ciemny", "Systemowy")

    // Obsługa błędów
    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage

        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            settingsViewModel.onErrorShown()
        }
    }

    if (showTimeDialog) {
        SettingsOptionPickerDialog(
            title = "Wybierz wyprzedzenie",
            options = timeOptions,
            selectedOption = uiState.notificationTimeOption,
            onOptionSelected = {
                settingsViewModel.onNotificationTimeChange(it)
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
                settingsViewModel.onThemeChange(it)
                showThemeDialog = false
            },
            onDismissRequest = { showThemeDialog = false }
        )
    }
    // To jest stan, który przechowuje informację o tym, czy aplikacja ma uprawnienia do wysyłania powiadomień.
    // Jego początkowa wartość jest pobierana za pomocą checkNotificationPermission(context),
    // która sprawdza stan uprawnień przy pierwszym uruchomieniu.

    var hasNotificationPermission by remember {
        mutableStateOf(checkNotificationPermission(context))
    }

    // Obserwujemy zmiany w cyklu życia, aby zaktualizować stan uprawnień, gdy aplikacja wraca na pierwszy plan.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission = checkNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Rejestrujemy launcher do obsługi prośby o uprawnienia do powiadomień.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                settingsViewModel.setNotificationsEnabled(true)
            } else {
                settingsViewModel.setNotificationsEnabled(false)
                settingsViewModel.setErrorMessage("Nie przyznano uprawnień")
            }
        }
    )

    val isToggleChecked = uiState.notificationsEnabled && hasNotificationPermission

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                checked = isToggleChecked,
                                onCheckedChange = { isChecked ->
                                    // Jeśli użytkownik włącza powiadomienia, ale nie mamy uprawnień, prosimy o nie.
                                    settingsViewModel.onNotificationToggleRequested(isChecked) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
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
                            settingsViewModel.onLogout()
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



