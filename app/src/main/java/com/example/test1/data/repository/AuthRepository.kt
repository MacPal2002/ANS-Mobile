package com.example.test1.data.repository

import com.example.test1.data.models.RegisterData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val settingsRepository: SettingsRepository,
    private val functions: FirebaseFunctions
) {

    /**
     * Pobiera e-mail studenta na podstawie numeru albumu.
     */
    suspend fun getEmailForAlbumNumber(albumNumber: String): Result<String> = try {
        val document = firestore.collection("student_lookups")
            .document(albumNumber)
            .get()
            .await()

        if (document != null && document.exists()) {
            val email = document.getString("email")
            if (email != null) {
                Result.success(email)
            } else {
                Result.failure(Exception("Błąd danych: brak e-maila dla studenta."))
            }
        } else {
            Result.failure(Exception("Student o podanym numerze albumu nie istnieje."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Loguje użytkownika i zapisuje dane urządzenia.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Nie udało się uzyskać ID użytkownika po zalogowaniu."))

            settingsRepository.saveDeviceData(userId)
            settingsRepository.updateLastLogin(userId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerStudent(data: RegisterData): Result<Unit> = try {
        val params = hashMapOf(
            "email" to data.email,
            "password" to data.password,
            "albumNumber" to data.albumNumber,
            "verbisPassword" to data.verbisPassword
        )

        functions
            .getHttpsCallable("registerStudent")
            .call(params)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getAuthStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)

        // Ta część zostanie wywołana, gdy strumień zostanie zamknięty (np. ViewModel zniszczony)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Zwraca ID aktualnego użytkownika.
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Wylogowuje użytkownika i usuwa dane urządzenia.
     */
    suspend fun signOut(): Result<Unit> = try {
        // Najpierw usuwamy dane urządzenia, póki znamy userId
        getCurrentUserId()?.let { userId ->
            settingsRepository.removeDeviceData(userId)
        }
        // Następnie wylogowujemy
        auth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}