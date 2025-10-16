package com.example.test1.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File

/**
 * Rozpoczyna pobieranie pliku APK za pomocą systemowego DownloadManagera.
 * @param context Kontekst aplikacji.
 * @param url Adres URL do pliku APK.
 * @param version Numer nowej wersji, używany w tytule powiadomienia.
 * @return ID pobierania, które można użyć do jego śledzenia.
 */
fun startApkDownload(context: Context, url: String, version: String): Long {
    val destinationFile = "update.apk"

    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle("Pobieranie aktualizacji: $version") // Tytuł widoczny w powiadomieniu
        .setDescription("Pobieranie nowej wersji aplikacji...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, destinationFile)
        .setMimeType("application/vnd.android.package-archive")
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    val oldFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), destinationFile)
    if (oldFile.exists()) {
        oldFile.delete()
    }

    return downloadManager.enqueue(request)
}