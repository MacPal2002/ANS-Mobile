package com.example.test1.data.repository

import android.util.Log
import com.example.test1.data.models.GroupNode
import com.example.test1.data.models.ObservedGroup
import com.example.test1.data.local.ScheduleItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import com.google.firebase.functions.FirebaseFunctionsException
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import com.example.test1.data.local.ScheduleItemDao
import com.example.test1.data.models.ResultObject
import com.example.test1.util.castList
import com.example.test1.util.toTimestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import org.json.JSONObject

@Singleton
class ScheduleRepository @Inject constructor(
    private val scheduleDao: ScheduleItemDao,
    private val functions: FirebaseFunctions,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val jsonParser = Json {
        ignoreUnknownKeys = true // Ignoruje nieznane pola w JSON
        isLenient = true         // Pozwala na pewne luźniejsze formatowanie JSON
    }

    fun getObservedGroupsFlow(): Flow<Result<List<ObservedGroup>>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(Result.failure(Exception("Użytkownik nie jest zalogowany.")))
            close()
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection("student_observed_groups").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("DEBUG", "Listener error: ", error) // LOG BŁĘDU
                    trySend(Result.failure(error))
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    // LOG 1: Sprawdzamy, co jest w całym dokumencie
                    Log.d("DEBUG", "Snapshot received. Data: ${snapshot.data}")

                    val rawData = snapshot.get("groups")

                    val groupIds = (rawData as? List<*>)
                        ?.mapNotNull { item ->
                            Log.d("DEBUG", "Processing item: $item, Type: ${item?.javaClass?.name}")
                            // Używamy bezpieczniejszego bloku 'when' do konwersji
                            when (item) {
                                is Number -> item.toInt()
                                else -> null
                            }
                        }
                        ?: emptyList()

                    if (groupIds.isNotEmpty()) {
                        launch {
                            val detailsResult = getGroupDetails(groupIds)
                            trySend(detailsResult)
                        }
                    } else {
                        trySend(Result.success(emptyList()))
                    }
                } else {
                    Log.w("DEBUG", "Snapshot does not exist or is null.")
                }
            }

        awaitClose { listenerRegistration.remove() }
    }


    private suspend fun getGroupDetails(groupIds: List<Int>): Result<List<ObservedGroup>> {
        return try {
            if (groupIds.isEmpty()) return Result.success(emptyList())

            val documents = firestore.collection("group_details")
                .whereIn(FieldPath.documentId(), groupIds.map { it.toString() })
                .get()
                .await()

            val groups = documents.map { doc ->
                ObservedGroup(
                    // ZMIANA #3: ID grupy bierzemy z ID dokumentu i zamieniamy z powrotem na liczbę.
                    id = doc.id.toIntOrNull() ?: -1,
                    // Nazwę grupy bierzemy z pola "groupName".
                    name = doc.getString("groupName") ?: "Brak nazwy"
                )
            }
            Result.success(groups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun getAllDeanGroups(): Result<List<GroupNode>> {
        return try {
            val result = functions.getHttpsCallable("getAllDeanGroups").call().await()

            val dataMap = result.data as? Map<*, *>
            if (dataMap != null) {
                val jsonString = JSONObject(dataMap).toString()

                // --- TUTAJ DODAJESZ LOGOWANIE ---
                // "ServerResponse" to tag, po którym łatwo znajdziesz log w Logcat.
                Log.d("ServerResponse", "Odpowiedź z getAllDeanGroups: $jsonString")
                // ------------------------------------

                val resultObject = jsonParser.decodeFromString<ResultObject>(jsonString)
                val groupTree = resultObject.tree

                Result.success(groupTree)
            } else {
                // Możesz też zalogować, gdy dane są puste
                Log.w("ServerResponse", "Otrzymano puste (null) dane z serwera.")
                Result.failure(Exception("Otrzymano puste lub niepoprawne dane."))
            }

        } catch (e: Exception) {
            // Dobrą praktyką jest też logowanie samego błędu
            Log.e("ServerResponse", "Błąd podczas pobierania danych: ", e)

            // Twoja obsługa błędów pozostaje bez zmian
            if (e is kotlinx.serialization.SerializationException) {
                Result.failure(Exception("Błąd przetwarzania danych z serwera: ${e.message}"))
            } else if (e is FirebaseFunctionsException &&
                (e.code == FirebaseFunctionsException.Code.UNAVAILABLE || e.code == FirebaseFunctionsException.Code.INTERNAL)) {
                Result.failure(Exception("Brak połączenia z internetem. Sprawdź sieć i spróbuj ponownie."))
            } else {
                Result.failure(e)
            }
        }
    }
    suspend fun saveObservedGroups(groupIds: List<Int>): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Użytkownik nie jest zalogowany."))
        return try {
            firestore.collection("student_observed_groups").document(userId).update("groups", groupIds).await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is FirebaseFunctionsException &&
                (e.code == FirebaseFunctionsException.Code.UNAVAILABLE || e.code == FirebaseFunctionsException.Code.INTERNAL)) {

                Result.failure(Exception("Brak połączenia z internetem. Sprawdź sieć i spróbuj ponownie."))
            } else {
                // Dla wszystkich innych błędów, zwracamy oryginalny komunikat
                Result.failure(e)
            }
        }
    }

    // Pobiera listę ID obserwowanych grup z profilu użytkownika
    suspend fun getObservedGroupIds(): Result<List<Int>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Użytkownik nie jest zalogowany."))
        return try {
            val document = firestore.collection("student_observed_groups").document(userId).get().await()
            val rawData = document.get("groups")

            val groupIds = (rawData as? List<*>)
                ?.mapNotNull { (it as? Long)?.toInt() }
                ?: emptyList()

            Result.success(groupIds)
        } catch (e: Exception) {
            if (e is FirebaseFunctionsException &&
                (e.code == FirebaseFunctionsException.Code.UNAVAILABLE || e.code == FirebaseFunctionsException.Code.INTERNAL)) {

                Result.failure(Exception("Brak połączenia z internetem. Sprawdź sieć i spróbuj ponownie."))
            } else {
                // Dla wszystkich innych błędów, zwracamy oryginalny komunikat
                Result.failure(e)
            }
        }
    }


    /**
     * Pobiera dzienny plan zajęć w sposób reaktywny.
     * Zwraca Flow, który najpierw emituje dane z cache, a następnie próbuje
     * pobrać świeże dane z sieci, zaktualizować cache i wyemitować nową listę.
     */
    fun getDailySchedule(groupId: Int, date: LocalDate): Flow<Result<List<ScheduleItem>>> = flow {
        // 1. Natychmiast emituj dane z lokalnej bazy (cache)
        val cachedSchedule = scheduleDao.getScheduleByGroupAndDate(groupId, date)
        emit(Result.success(cachedSchedule))

        // 2. Spróbuj pobrać świeże dane z sieci w tle
        try {
            val remoteSchedule = fetchScheduleFromFirebase(groupId, date)

            // 3. Zaktualizuj bazę danych
            scheduleDao.deleteScheduleByGroupAndDate(groupId, date)
            scheduleDao.insertSchedule(remoteSchedule)

            // 4. Pobierz nowe dane z bazy (jedyne źródło prawdy) i wyemituj je ponownie
            val newScheduleFromDb = scheduleDao.getScheduleByGroupAndDate(groupId, date)
            emit(Result.success(newScheduleFromDb))
        } catch (e: Exception) {
            // 5. W przypadku błędu sieci, wyemituj błąd.
            // UI wciąż będzie miało ostatnie dane z cache, które dostało na początku.
            Log.e(
                "ScheduleRepo",
                "Failed to fetch remote schedule. Exception type: ${e.javaClass.simpleName}, Message: ${e.message}",
                e // Zostawiamy 'e', aby zobaczyć pełny stack trace
            )
            emit(handleFirebaseException(e))
        }
    }

    /**
     * Prywatna funkcja do pobierania danych bezpośrednio z Firebase Cloud Functions.
     */
    private suspend fun fetchScheduleFromFirebase(groupId: Int, date: LocalDate): List<ScheduleItem> {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val data = hashMapOf(
            "groupId" to groupId,
            "dateString" to dateString
        )

        val result = functions
            .getHttpsCallable("getDailySchedule")
            .call(data)
            .await()

        // Bezpieczne rzutowanie elementów listy
        val scheduleData = (result.data as? Map<*, *>)?.get("schedule")
            .castList<Map<String, Any>>()

        return scheduleData.map { mapToScheduleItem(it, groupId, date) }
    }


    /**
     * Mapuje surowe dane (Map) z Firebase na obiekt encji ScheduleItem.
     */
    private fun mapToScheduleItem(data: Map<String, Any>, groupId: Int, date: LocalDate): ScheduleItem {
        val startTime = (data["startTime"] as? Map<*, *>)?.toTimestamp() ?: Timestamp.now()
        val endTime = (data["endTime"] as? Map<*, *>)?.toTimestamp() ?: Timestamp.now()

        return ScheduleItem(
            groupId = groupId,
            date = date,
            subjectFullName = data["subjectFullName"] as? String ?: "Brak nazwy",
            classType = data["classType"] as? String,
            startTime = startTime,
            endTime = endTime,
            lecturers = (data["lecturers"]).castList<Map<String, Any>>(),
            rooms = (data["rooms"]).castList<Map<String, Any>>()
        )
    }



    /**
     * Centralna funkcja do obsługi błędów z Firebase.
     * Tworzy czytelny komunikat dla użytkownika w przypadku problemów z siecią.
     */
    private fun <T> handleFirebaseException(e: Exception): Result<T> {
        val finalException = if (e is FirebaseFunctionsException &&
            (e.code == FirebaseFunctionsException.Code.UNAVAILABLE || e.code == FirebaseFunctionsException.Code.INTERNAL)) {
            Exception("Brak połączenia z internetem. Sprawdź sieć i spróbuj ponownie.", e)
        } else {
            e
        }
        return Result.failure(finalException)
    }
}