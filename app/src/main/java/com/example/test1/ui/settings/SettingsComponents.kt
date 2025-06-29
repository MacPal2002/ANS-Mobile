package com.example.test1.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val primaryColor = Color(0xFF212C5D)
val secondaryColor = Color(0xFFE8EAF6)
val primaryContentColor = Color.White
val textColor = Color(0xFF1F1F1F)
val subtextColor = Color.Gray

@Composable
fun SettingsSection(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(secondaryColor)
            .padding(vertical = 16.dp),
    ) {
        content()
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = subtextColor,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}


@Preview
@Composable
fun CreativeAppInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = secondaryColor),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(primaryColor)
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "App Logo",
                        tint = primaryContentColor,
                        modifier = Modifier.size(56.dp)
                    )
                    Column {
                        Text(
                            text = "ANS NT App",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryContentColor
                        )
                        Text(
                            text = "Wersja 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = primaryContentColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                InfoDetailRow(icon = Icons.Default.Person, label = "Autor", value = "Maciej Palenica")
                InfoDetailRow(icon = Icons.Default.Email, label = "Kontakt", value = "13815@eans-nt.edu.pl")
            }

            HorizontalDivider(color = Color.Black.copy(alpha = 0.08f), thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Logika kliknięcia */ }
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Polityka Prywatności",
                    tint = primaryColor
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Polityka prywatności",
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = subtextColor
                )
            }
        }
    }
}


@Composable
private fun InfoDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = primaryColor,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = subtextColor)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = textColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, color = textColor, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primaryColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}


@Composable
fun SettingsClickableRow(
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = textColor, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(16.dp))
        if (value != null) {
            Text(text = value, color = primaryColor, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = subtextColor
        )
    }
}

@Composable
fun ProfileHeader(fullName: String, albumNumber: String, deanGroups: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp), // Odstęp od TopBar
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Okrągły awatar
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(secondaryColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                tint = primaryColor,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Imię i nazwisko
        Text(
            text = fullName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontFamily = interFontFamily
        )

        // Numer albumu
        Text(
            text = "Nr albumu: $albumNumber",
            fontSize = 16.sp,
            color = subtextColor,
            fontFamily = interFontFamily
        )

        // Grupy dziekańskie
        Text(
            text = "Grupy: ${deanGroups.joinToString(", ")}",
            fontSize = 14.sp,
            color = subtextColor,
            fontFamily = interFontFamily,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SettingsOptionPickerDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(20.dp),
        containerColor = secondaryColor,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontFamily = interFontFamily,
                color = primaryColor
            )
        },
        text = {
            Column(Modifier.selectableGroup()) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    val rowBackgroundColor = if (isSelected) primaryColor.copy(alpha = 0.1f) else Color.Transparent
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(rowBackgroundColor)
                            .selectable(
                                selected = isSelected,
                                onClick = { onOptionSelected(option) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                        )
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = interFontFamily,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(contentColor = primaryColor)
            ) {
                Text("GOTOWE", fontWeight = FontWeight.Bold, fontFamily = interFontFamily)
            }
        }
    )
}