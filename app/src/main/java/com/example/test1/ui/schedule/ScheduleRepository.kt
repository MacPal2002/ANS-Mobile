package com.example.test1.ui.schedule

import android.util.Log
import com.example.test1.data.GroupNode
import com.example.test1.data.ScheduleItem
import com.google.firebase.Timestamp
import com.google.firebase.functions.ktx.functions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import com.google.firebase.functions.FirebaseFunctionsException
import java.time.format.DateTimeFormatter

class ScheduleRepository {
    private val functions = Firebase.functions("europe-central2")
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

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

    // Pobiera nazwy grup na podstawie ich ID
    suspend fun getGroupDetails(groupIds: List<Int>): Result<List<ObservedGroup>> {
        if (groupIds.isEmpty()) return Result.success(emptyList())

        val data = hashMapOf("groupIds" to groupIds)
        return try {
            val result = functions.getHttpsCallable("getGroupDetails").call(data).await()
            val groupsData = result.data as? Map<*, *>
            val groupList = groupsData?.get("groups") as? List<Map<String, Any>> ?: emptyList()
            val observedGroups = groupList.map {
                ObservedGroup(
                    id = (it["id"] as? Number)?.toInt() ?: 0,
                    name = it["name"] as? String ?: "B/N"
                )
            }
            Result.success(observedGroups)
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