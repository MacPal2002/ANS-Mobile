package com.example.test1.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Scaffold
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test1.R
import com.example.test1.ui.component.AppTopBar


val destructiveColor = Color(0xFFD32F2F)


val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val interFontFamily = FontFamily(
    Font(GoogleFont("Inter"), provider)
)

@Composable
fun SettingsScreen(
    onLogout: () -> Unit
) {
    val timeOptions = listOf("15 minut", "30 minut", "1 godzina", "2 godziny")
    var selectedTime by remember { mutableStateOf(timeOptions[2]) }
    var showTimeDialog by remember { mutableStateOf(false) }

    val themeOptions = listOf("Jasny", "Ciemny", "Systemowy")
    var selectedTheme by remember { mutableStateOf(themeOptions[2]) }
    var showThemeDialog by remember { mutableStateOf(false) }

    var notificationsEnabled by remember { mutableStateOf(true) }

    if (showTimeDialog) {
        SettingsOptionPickerDialog(
            title = "Wybierz wyprzedzenie",
            options = timeOptions,
            selectedOption = selectedTime,
            onOptionSelected = { newOption ->
                selectedTime = newOption
                showTimeDialog = false
            },
            onDismissRequest = {
                showTimeDialog = false
            }
        )
    }

    if (showThemeDialog) {
        SettingsOptionPickerDialog(
            title = "Wybierz motyw aplikacji",
            options = themeOptions,
            selectedOption = selectedTheme,
            onOptionSelected = { newOption ->
                selectedTheme = newOption
                showThemeDialog = false
            },
            onDismissRequest = {
                showThemeDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationIconClick = { /* Handle back navigation */ },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ProfileHeader(
                    fullName = "Jan Kowalski",
                    albumNumber = "s12345",
                    deanGroups = listOf("W-11", "L-12", "C-13")
                )
            }

            item {
                Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                    SettingsSection {
                        SectionHeader(title = "PERSONALIZACJA")
                        SettingsClickableRow(title = "Motyw aplikacji",
                            value = selectedTheme,
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
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                        SettingsClickableRow(
                            title = "Wyprzedzenie",
                            value = selectedTime,
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
                    onClick = { onLogout() },
                    modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
                ) {
                    Text(
                        text = "Wyloguj się",
                        color = destructiveColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        fontFamily = interFontFamily,
                    )
                }
            }
        }
    }
}



