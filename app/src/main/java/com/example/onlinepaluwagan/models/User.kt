package com.example.onlinepaluwagan.models

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "", // "head" or "member"
    val createdAt: Long = 0,
    val roleSelectedAt: Long = 0,
    val activeGroups: List<String> = listOf(), // List of group IDs
    val totalFunds: Double = 0.0,
    val profileImageUrl: String = "",
    val fcmToken: String = "", // For push notifications
    val isVerified: Boolean = false
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "fullName" to fullName,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "role" to role,
            "createdAt" to createdAt,
            "roleSelectedAt" to roleSelectedAt,
            "activeGroups" to activeGroups,
            "totalFunds" to totalFunds,
            "profileImageUrl" to profileImageUrl,
            "fcmToken" to fcmToken,
            "isVerified" to isVerified
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): User {
            return User(
                id = map["id"] as? String ?: "",
                fullName = map["fullName"] as? String ?: "",
                email = map["email"] as? String ?: "",
                phoneNumber = map["phoneNumber"] as? String ?: "",
                role = map["role"] as? String ?: "",
                createdAt = map["createdAt"] as? Long ?: 0L,
                roleSelectedAt = map["roleSelectedAt"] as? Long ?: 0L,
                activeGroups = (map["activeGroups"] as? List<*>)?.filterIsInstance<String>() ?: listOf(),
                totalFunds = (map["totalFunds"] as? Number)?.toDouble() ?: 0.0,
                profileImageUrl = map["profileImageUrl"] as? String ?: "",
                fcmToken = map["fcmToken"] as? String ?: "",
                isVerified = map["isVerified"] as? Boolean ?: false
            )
        }
    }
}
