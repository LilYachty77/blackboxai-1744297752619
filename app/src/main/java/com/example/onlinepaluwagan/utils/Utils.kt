package com.example.onlinepaluwagan.utils

import android.content.Context
import android.text.format.DateFormat
import android.util.Patterns
import android.widget.Toast
import java.text.NumberFormat
import java.util.*

object Utils {
    // Validation
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPhilippinesPhone(phone: String): Boolean {
        // Philippines mobile number format: 9XXXXXXXXX (10 digits starting with 9)
        return phone.matches(Regex("^9\\d{9}$"))
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2
    }

    // Formatting
    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("PHP")
    }

    fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount)
    }

    fun formatDate(timestamp: Long, pattern: String = "MMM dd, yyyy"): String {
        return DateFormat.format(pattern, timestamp).toString()
    }

    fun formatDateTime(timestamp: Long): String {
        return DateFormat.format("MMM dd, yyyy hh:mm a", timestamp).toString()
    }

    fun formatPhoneNumber(phone: String): String {
        return if (phone.length == 10) {
            "+63 ${phone.substring(0, 3)} ${phone.substring(3, 6)} ${phone.substring(6)}"
        } else {
            phone
        }
    }

    // Time Utils
    fun getDaysUntil(timestamp: Long): Int {
        val diff = timestamp - System.currentTimeMillis()
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun getNextCollectionDate(frequency: String, startDate: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = startDate }
        
        when (frequency) {
            "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "BIWEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 2)
            "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
        }
        
        return calendar.timeInMillis
    }

    // UI Utils
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    fun getProgressColor(progress: Float): Int {
        return when {
            progress < 25 -> android.graphics.Color.RED
            progress < 50 -> android.graphics.Color.parseColor("#FFA500") // Orange
            progress < 75 -> android.graphics.Color.parseColor("#FFD700") // Gold
            else -> android.graphics.Color.parseColor("#4CAF50") // Green
        }
    }

    fun getStatusColor(status: String): Int {
        return when (status) {
            "PENDING" -> android.graphics.Color.parseColor("#FFA500") // Orange
            "PAID" -> android.graphics.Color.parseColor("#4CAF50") // Green
            "OVERDUE" -> android.graphics.Color.RED
            "CANCELLED" -> android.graphics.Color.GRAY
            else -> android.graphics.Color.BLACK
        }
    }

    // Error Handling
    fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is java.net.UnknownHostException -> "No internet connection"
            is java.net.SocketTimeoutException -> "Connection timed out"
            is com.google.firebase.FirebaseNetworkException -> "Network error"
            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid credentials"
            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "Account not found"
            is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "Password is too weak"
            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "Email already in use"
            else -> exception.message ?: "An error occurred"
        }
    }

    // Constants
    const val COLLECTION_USERS = "users"
    const val COLLECTION_GROUPS = "groups"
    const val COLLECTION_COLLECTIONS = "collections"

    const val ROLE_HEAD = "head"
    const val ROLE_MEMBER = "member"

    const val MAX_GROUP_MEMBERS = 12
    const val MIN_CONTRIBUTION_AMOUNT = 100.0
    const val MAX_CONTRIBUTION_AMOUNT = 50000.0

    const val REMINDER_THRESHOLD_DAYS = 3
}
