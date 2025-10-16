package com.example.test1

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ScheduleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Idealne miejsce na jednorazowe operacje
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channelId = "upcoming_class_channel"
        val channel = NotificationChannel(
            channelId,
            "Nadchodzące zajęcia",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}