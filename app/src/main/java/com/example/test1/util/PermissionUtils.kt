package com.example.test1.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.net.Uri
import android.provider.Settings

// To jest teraz publiczna funkcja, dostępna w całej aplikacji
fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // Na starszych wersjach uprawnienia są zawsze "przyznane"
        true
    }
}

/**
 * Sprawdza, czy aplikacja ma uprawnienie do instalowania pakietów.
 * @return `true` jeśli uprawnienie jest przyznane, w przeciwnym razie `false`.
 */
fun canRequestPackageInstalls(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.packageManager.canRequestPackageInstalls()
    } else {
        // Na starszych wersjach to uprawnienie nie jest wymagane
        true
    }
}

/**
 * Otwiera ekran ustawień systemowych, gdzie użytkownik może manualnie
 * przyznać aplikacji uprawnienie do instalowania z nieznanych źródeł.
 */
fun requestInstallPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}