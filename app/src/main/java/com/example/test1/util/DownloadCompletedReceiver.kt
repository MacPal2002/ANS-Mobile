package com.example.test1.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.test1.util.canRequestPackageInstalls
import com.example.test1.util.requestInstallPermission

class DownloadCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id != -1L) {
                if (canRequestPackageInstalls(context)) {
                    startInstallation(context, id)
                } else {
                    Toast.makeText(context, "Wymagane pozwolenie. Włącz je w ustawieniach.", Toast.LENGTH_LONG).show()
                    requestInstallPermission(context)
                }
            }
        }
    }

    /**
     * Funkcja pomocnicza, która uruchamia instalator APK.
     */
    private fun startInstallation(context: Context, downloadId: Long) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = downloadManager.getUriForDownloadedFile(downloadId)

        if (uri != null) {
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(installIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Nie można uruchomić instalatora.", Toast.LENGTH_LONG).show()
            }
        }
    }
}