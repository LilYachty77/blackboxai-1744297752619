package com.example.onlinepaluwagan.models

data class Collection(
    val id: String = "",
    val groupId: String = "",
    val userId: String = "", // Member who needs to pay
    val amount: Double = 0.0,
    val dueDate: Long = 0,
    val paidDate: Long? = null,
    val status: String = STATUS_PENDING,
    val paymentMethod: String = "",
    val paymentReference: String = "",
    val round: Int = 1,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val reminderSent: Boolean = false,
    val lastReminderDate: Long = 0,
    val notes: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "groupId" to groupId,
            "userId" to userId,
            "amount" to amount,
            "dueDate" to dueDate,
            "paidDate" to paidDate,
            "status" to status,
            "paymentMethod" to paymentMethod,
            "paymentReference" to paymentReference,
            "round" to round,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "reminderSent" to reminderSent,
            "lastReminderDate" to lastReminderDate,
            "notes" to notes
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Collection {
            return Collection(
                id = map["id"] as? String ?: "",
                groupId = map["groupId"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
                dueDate = map["dueDate"] as? Long ?: 0L,
                paidDate = map["paidDate"] as? Long,
                status = map["status"] as? String ?: STATUS_PENDING,
                paymentMethod = map["paymentMethod"] as? String ?: "",
                paymentReference = map["paymentReference"] as? String ?: "",
                round = (map["round"] as? Number)?.toInt() ?: 1,
                createdAt = map["createdAt"] as? Long ?: 0L,
                updatedAt = map["updatedAt"] as? Long ?: 0L,
                reminderSent = map["reminderSent"] as? Boolean ?: false,
                lastReminderDate = map["lastReminderDate"] as? Long ?: 0L,
                notes = map["notes"] as? String ?: ""
            )
        }

        const val STATUS_PENDING = "PENDING"
        const val STATUS_PAID = "PAID"
        const val STATUS_OVERDUE = "OVERDUE"
        const val STATUS_CANCELLED = "CANCELLED"

        const val PAYMENT_METHOD_GCASH = "GCASH"
        const val PAYMENT_METHOD_PAYMAYA = "PAYMAYA"
        const val PAYMENT_METHOD_BANK = "BANK"
        const val PAYMENT_METHOD_CASH = "CASH"
    }

    val isPending: Boolean
        get() = status == STATUS_PENDING

    val isPaid: Boolean
        get() = status == STATUS_PAID

    val isOverdue: Boolean
        get() = status == STATUS_OVERDUE

    val isCancelled: Boolean
        get() = status == STATUS_CANCELLED

    val isLate: Boolean
        get() = dueDate < System.currentTimeMillis() && !isPaid

    val needsReminder: Boolean
        get() = !reminderSent && isLate && !isPaid && !isCancelled

    val daysOverdue: Int
        get() {
            if (!isLate) return 0
            val diff = System.currentTimeMillis() - dueDate
            return (diff / (1000 * 60 * 60 * 24)).toInt()
        }

    val formattedStatus: String
        get() = when (status) {
            STATUS_PENDING -> "Pending"
            STATUS_PAID -> "Paid"
            STATUS_OVERDUE -> "Overdue"
            STATUS_CANCELLED -> "Cancelled"
            else -> status
        }

    val formattedPaymentMethod: String
        get() = when (paymentMethod) {
            PAYMENT_METHOD_GCASH -> "GCash"
            PAYMENT_METHOD_PAYMAYA -> "PayMaya"
            PAYMENT_METHOD_BANK -> "Bank Transfer"
            PAYMENT_METHOD_CASH -> "Cash"
            else -> paymentMethod
        }
}
