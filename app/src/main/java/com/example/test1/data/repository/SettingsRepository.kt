package com.example.test1.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.test1.data.models.StudentDevices
import com.example.test1.data.models.UserSettings
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val firestore: FirebaseFirestore,
    private val installations: FirebaseInstallations
) {

    private companion object {
        const val USER_DEVICES_COLLECTION = "student_devices"
        const val STUDENTS_COLLECTION = "students"
        const val DEVICES_FIELD = "devices"
    }

    private val themePreferenceKey = stringPreferencesKey("theme_option")

    val themeOptionFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[themePreferenceKey] ?: "Systemowy"
        }

    suspend fun saveThemeOption(theme: String) {
        dataStore.edit { settings ->
            settings[themePreferenceKey] = theme
        }
    }

    /**
     * Aktualizuje pole lastLogin w dokumencie użytkownika w kolekcji 'students'.
     */
    suspend fun updateLastLogin(userId: String): Result<Unit> = try {
        val currentTime = Timestamp.now()
        firestore.collection(STUDENTS_COLLECTION).document(userId)
            .update("lastLogin", currentTime).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.w("SettingsRepository", "Błąd podczas aktualizacji lastLogin", e)
        Result.failure(e)
    }
    /**
     * Zapisuje dane urządzenia. Tworzy nowy wpis z domyślnymi ustawieniami
     * lub aktualizuje tylko token, jeśli urządzenie już istnieje.
     */
    suspend fun saveDeviceData(userId: String): Result<Unit> = try {
        val deviceId = installations.id.await()
        val token = Firebase.messaging.token.await() // Zakładając, że masz tę zależność
        val userDevicesRef = firestore.collection(USER_DEVICES_COLLECTION).document(userId)
        val docSnapshot = userDevicesRef.get().await()

        if (docSnapshot.exists() && docSnapshot.get("$DEVICES_FIELD.$deviceId") != null) {
            // Urządzenie istnieje - aktualizuj tylko token
            userDevicesRef.update("$DEVICES_FIELD.$deviceId.token", token).await()
        } else {
            // Nowe urządzenie - stwórz z domyślnymi ustawieniami
            val newDeviceData = mapOf(
                DEVICES_FIELD to mapOf(
                    deviceId to mapOf(
                        "token" to token,
                        "notificationEnabled" to false,
                        "notificationTimeOption" to "15 minut"
                    )
                )
            )
            userDevicesRef.set(newDeviceData, SetOptions.merge()).await()
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.w("SettingsRepository", "Błąd podczas zapisu danych urządzenia", e)
        Result.failure(e)
    }

    /**
     * Pobiera połączone dane użytkownika z kolekcji 'students' i 'userDevices'.
     */
    suspend fun getUserData(userId: String): Result<UserSettings> = try {
        val deviceId = installations.id.await()
        val userDoc = firestore.collection(STUDENTS_COLLECTION).document(userId).get().await()
        val userDeviceDoc = firestore.collection(USER_DEVICES_COLLECTION).document(userId).get().await()

        if (userDoc.exists()) {
            val studentDevices = userDeviceDoc.toObject(StudentDevices::class.java)
            val deviceSettings = studentDevices?.devices?.get(deviceId)
            val notificationsEnabled = deviceSettings?.notificationEnabled ?: false
            val notificationTime = deviceSettings?.notificationTimeOption ?: "N/A"

            Result.success(
                UserSettings(
                    displayName = userDoc.getString("displayName") ?: "Brak nazwy",
                    albumNumber = userDoc.getString("albumNumber") ?: "Brak albumu",
                    notificationsEnabled = notificationsEnabled,
                    notificationTimeOption = notificationTime
                )
            )
        } else {
            Result.failure(Exception("Nie znaleziono danych użytkownika."))
        }
    } catch (e: Exception) {
        Log.w("SettingsRepository", "Błąd podczas pobierania danych użytkownika", e)
        Result.failure(e)
    }

    /**
     * Aktualizuje pojedyncze ustawienie dla bieżącego urządzenia.
     */
    suspend fun updateDeviceSetting(userId: String, key: String, value: Any): Result<Unit> = try {
        val deviceId = installations.id.await()
        val settingPath = "$DEVICES_FIELD.$deviceId.$key"
        firestore.collection(USER_DEVICES_COLLECTION).document(userId).update(settingPath, value).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("SettingsRepository", "Błąd podczas aktualizacji ustawienia", e)
        Result.failure(e)
    }

    /**
     * Usuwa dane bieżącego urządzenia z dokumentu użytkownika.
     */
    suspend fun removeDeviceData(userId: String): Result<Unit> = try {
        val deviceId = installations.id.await()
        val devicePath = "$DEVICES_FIELD.$deviceId"
        firestore.collection(USER_DEVICES_COLLECTION).document(userId)
            .update(devicePath, FieldValue.delete()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.w("SettingsRepository", "Błąd podczas usuwania danych urządzenia", e)
        Result.failure(e)
    }
}
