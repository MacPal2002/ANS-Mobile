package com.example.test1.ui.component

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.github.javiersantos.appupdater.objects.Update
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.test1.util.startApkDownload

@Composable
fun UpdateDialog(update: Update, onDismiss: () -> Unit) {
    val context = LocalContext.current
    // Stan do śledzenia, czy pobieranie się rozpoczęło
    val isDownloading = remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isDownloading.value) onDismiss() }, // Nie zamykaj, gdy trwa pobieranie
        title = { Text("Dostępna aktualizacja!") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Nowa wersja: ${update.latestVersion}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                update.releaseNotes?.let { notes ->
                    if (notes.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Opis zmian:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val version = update.latestVersion // np. "1.2.1"
                    // 1. Ręcznie budujemy poprawny, bezpośredni link do pobrania
                    val directDownloadUrl = "https://github.com/MacPal2002/ans-nt-app_release/releases/download/v$version/app-v$version.apk"
                    // 2. Logujemy go, aby mieć pewność, że jest poprawny
                    Log.d("UpdateDialog", "Ręcznie zbudowany link: $directDownloadUrl")
                    // 3. Używamy NASZEGO linku do rozpoczęcia pobierania
                    startApkDownload(context, directDownloadUrl, version)
                    isDownloading.value = true
                },
                enabled = !isDownloading.value // Wyłącz przycisk po rozpoczęciu pobierania
            ) {
                if (isDownloading.value) {
                    Text("Pobieranie...")
                } else {
                    Text("Aktualizuj")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDownloading.value // Wyłącz przycisk po rozpoczęciu pobierania
            ) {
                Text("Później")
            }
        }
    )
}