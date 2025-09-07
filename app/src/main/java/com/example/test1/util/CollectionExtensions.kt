package com.example.test1.util

import com.google.firebase.Timestamp

/**
 * Extension do bezpiecznego rzutowania Any? na List<T>
 */
inline fun <reified T> Any?.castList(): List<T> =
    (this as? List<*>)?.mapNotNull { it as? T } ?: emptyList()

/**
 * Extension do konwersji Map na Timestamp
 */
fun Map<*, *>.toTimestamp(): Timestamp {
    val seconds = (this["_seconds"] as? Number)?.toLong() ?: 0L
    val nanos = (this["_nanoseconds"] as? Number)?.toInt() ?: 0
    return Timestamp(seconds, nanos)
}