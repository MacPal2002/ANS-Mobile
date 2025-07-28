package com.example.test1.data

data class GroupNode(
    val id: String,
    val name: String,
    val type: String,
    val children: List<GroupNode> = emptyList(),
    // Dla liści - przechowuje ID jako Int
    val groupId: Int? = (id as? Double)?.toInt() ?: try { id.toInt() } catch (e: NumberFormatException) { null }
)