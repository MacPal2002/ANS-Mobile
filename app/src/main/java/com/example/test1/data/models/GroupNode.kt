package com.example.test1.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GroupNode(
    val id: String,
    val name: String,
    val type: String,
    val children: List<GroupNode> = emptyList(),
    val groupId: Int? = null
)

@Serializable
data class ResultObject(
    val tree: List<GroupNode>
)

