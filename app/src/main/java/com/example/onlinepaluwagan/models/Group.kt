package com.example.onlinepaluwagan.models

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val headId: String = "", // User ID of the group head
    val members: List<String> = listOf(), // List of member User IDs
    val contributionAmount: Double = 0.0, // Amount each member needs to contribute
    val frequency: String = "MONTHLY", // WEEKLY, BIWEEKLY, MONTHLY
    val startDate: Long = 0,
    val endDate: Long = 0,
    val totalFunds: Double = 0.0,
    val collectedFunds: Double = 0.0,
    val nextCollectionDate: Long = 0,
    val collectionOrder: List<String> = listOf(), // Ordered list of member IDs
    val currentRound: Int = 1,
    val totalRounds: Int = 1,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, CANCELLED
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val isPublic: Boolean = false,
    val maxMembers: Int = 12
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "description" to description,
            "headId" to headId,
            "members" to members,
            "contributionAmount" to contributionAmount,
            "frequency" to frequency,
            "startDate" to startDate,
            "endDate" to endDate,
            "totalFunds" to totalFunds,
            "collectedFunds" to collectedFunds,
            "nextCollectionDate" to nextCollectionDate,
            "collectionOrder" to collectionOrder,
            "currentRound" to currentRound,
            "totalRounds" to totalRounds,
            "status" to status,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "isPublic" to isPublic,
            "maxMembers" to maxMembers
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Group {
            return Group(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                description = map["description"] as? String ?: "",
                headId = map["headId"] as? String ?: "",
                members = (map["members"] as? List<*>)?.filterIsInstance<String>() ?: listOf(),
                contributionAmount = (map["contributionAmount"] as? Number)?.toDouble() ?: 0.0,
                frequency = map["frequency"] as? String ?: "MONTHLY",
                startDate = map["startDate"] as? Long ?: 0L,
                endDate = map["endDate"] as? Long ?: 0L,
                totalFunds = (map["totalFunds"] as? Number)?.toDouble() ?: 0.0,
                collectedFunds = (map["collectedFunds"] as? Number)?.toDouble() ?: 0.0,
                nextCollectionDate = map["nextCollectionDate"] as? Long ?: 0L,
                collectionOrder = (map["collectionOrder"] as? List<*>)?.filterIsInstance<String>() ?: listOf(),
                currentRound = (map["currentRound"] as? Number)?.toInt() ?: 1,
                totalRounds = (map["totalRounds"] as? Number)?.toInt() ?: 1,
                status = map["status"] as? String ?: "ACTIVE",
                createdAt = map["createdAt"] as? Long ?: 0L,
                updatedAt = map["updatedAt"] as? Long ?: 0L,
                isPublic = map["isPublic"] as? Boolean ?: false,
                maxMembers = (map["maxMembers"] as? Number)?.toInt() ?: 12
            )
        }

        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_CANCELLED = "CANCELLED"

        const val FREQUENCY_WEEKLY = "WEEKLY"
        const val FREQUENCY_BIWEEKLY = "BIWEEKLY"
        const val FREQUENCY_MONTHLY = "MONTHLY"
    }

    val isActive: Boolean
        get() = status == STATUS_ACTIVE

    val isCompleted: Boolean
        get() = status == STATUS_COMPLETED

    val isCancelled: Boolean
        get() = status == STATUS_CANCELLED

    val progress: Float
        get() = if (totalFunds > 0) (collectedFunds / totalFunds) * 100 else 0f

    val isFull: Boolean
        get() = members.size >= maxMembers

    val remainingSlots: Int
        get() = maxMembers - members.size
}
