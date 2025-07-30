package com.example.test1.ui.schedule

import android.util.Log
import com.example.test1.data.GroupNode
import com.example.test1.data.ObservedGroup
import com.example.test1.data.ScheduleItem
import com.google.firebase.Timestamp
import com.google.firebase.functions.ktx.functions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import com.google.firebase.functions.FirebaseFunctionsException
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class ScheduleRepository {
    private val functions = Firebase.functions("europe-central2")
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    // NOWA, REAKTYWNA FUNKCJA - NASZE GŁÓWNE NARZĘDZIE
    fun getObservedGroupsFlow(): Flow<Result<List<ObservedGroup>>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(Result.failure(Exception("Użytkownik nie jest zalogowany.")))
            close()
            return@callbackFlow
        }

        val listenerRegistration = db.collection("students").document(userId)
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

                    val rawData = snapshot.get("observedGroups")
                    // LOG 2: Sprawdzamy, co dokładnie jest w polu 'observedGroups' i jakiego jest typu
                    Log.d("DEBUG", "Raw 'observedGroups' data: $rawData, Type: ${rawData?.javaClass?.name}")

                    val groupIds = (rawData as? List<*>)
                        ?.mapNotNull { item ->
                            // LOG 3: Sprawdzamy każdy element w liście przed konwersją
                            Log.d("DEBUG", "Processing item: $item, Type: ${item?.javaClass?.name}")
                            // Używamy bezpieczniejszego bloku 'when' do konwersji
                            when (item) {
                                is Number -> item.toInt()
                                else -> null
                            }
                        }
                        ?: emptyList()

                    // LOG 4: Sprawdzamy, co udało się sparsować
                    Log.d("DEBUG", "Parsed groupIds: $groupIds")

                    if (groupIds.isNotEmpty()) {
                        launch {
                            val detailsResult = getGroupDetails(groupIds)
                            // LOG 5: Sprawdzamy, czy udało się pobrać szczegóły grup
                            Log.d("DEBUG", "getGroupDetails result: $detailsResult")
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


    suspend fun getGroupDetails(groupIds: List<Int>): Result<List<ObservedGroup>> {
        return try {
            if (groupIds.isEmpty()) return Result.success(emptyList())

            // ZMIANA #1: Szukamy w poprawnej kolekcji -> "groupDetails"
            // ZMIANA #2: Używamy FieldPath.documentId(), aby szukać po ID DOKUMENTU, a nie polu w środku.
            //            Musimy też zamienić listę liczb (Int) na listę tekstów (String), bo ID dokumentów to tekst.
            val documents = db.collection("groupDetails")
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
            val treeData = (result.data as? Map<*, *>)?.get("tree") as? List<Map<String, Any>>
            val groupTree = mapToGroupTree(treeData ?: emptyList())
            Result.success(groupTree)
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
    private fun mapToGroupTree(data: List<Map<String, Any>>): List<GroupNode> {
        return data.map { nodeMap ->
            GroupNode(
                id = nodeMap["id"].toString(),
                name = nodeMap["name"] as? String ?: "",
                type = nodeMap["type"] as? String ?: "",
                children = mapToGroupTree(nodeMap["children"] as? List<Map<String, Any>> ?: emptyList())
            )
        }
    }
    suspend fun saveObservedGroups(groupIds: List<Int>): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Użytkownik nie jest zalogowany."))
        return try {
            firestore.collection("students").document(userId).update("observedGroups", groupIds).await()
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
            val document = firestore.collection("students").document(userId).get().await()
            // Pobierz surowe dane z pola "observedGroups"
            val rawData = document.get("observedGroups")

            // Bezpiecznie rzutuj na listę i od razu zmapuj na List<Int>
            val groupIds = (rawData as? List<*>)
                ?.mapNotNull { (it as? Long)?.toInt() } // Ta sama logika mapowania co wcześniej
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


    suspend fun getDailySchedule(groupId: Int, date: LocalDate): Result<List<ScheduleItem>> {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE) // Format "YYYY-MM-DD"
        val data = hashMapOf(
            "groupId" to groupId,
            "dateString" to dateString
        )

        return try {
            val result = functions
                .getHttpsCallable("getDailySchedule")
                .call(data)
                .await()

            // Parsowanie wyniku
            val scheduleData = (result.data as? Map<*, *>)?.get("schedule") as? List<Map<String, Any>>
            val scheduleItems = scheduleData?.map { mapToScheduleItem(it) } ?: emptyList()

            Result.success(scheduleItems)
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

    // Funkcja do mapowania surowych danych z Firebase na model
    private fun mapToScheduleItem(data: Map<String, Any>): ScheduleItem {
        val startTimeMap = data["startTime"] as? Map<String, Number> ?: emptyMap()
        val endTimeMap = data["endTime"] as? Map<String, Number> ?: emptyMap()

        // Bezpiecznie konwertujemy 'Number' na wymagany typ (.toLong() i .toInt())
        val startTime = Timestamp(
            startTimeMap["_seconds"]?.toLong() ?: 0L,
            startTimeMap["_nanoseconds"]?.toInt() ?: 0
        )
        val endTime = Timestamp(
            endTimeMap["_seconds"]?.toLong() ?: 0L,
            endTimeMap["_nanoseconds"]?.toInt() ?: 0
        )

        return ScheduleItem(
            subjectFullName = data["subjectFullName"] as? String ?: "Brak nazwy",
            classType = data["classType"] as? String,
            startTime = startTime,
            endTime = endTime,
            lecturers = data["lecturers"] as? List<Map<String, Any>> ?: emptyList(),
            rooms = data["rooms"] as? List<Map<String, Any>> ?: emptyList()
        )
    }
}