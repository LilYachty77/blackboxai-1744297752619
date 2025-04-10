package com.example.onlinepaluwagan

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class PaluwaganApp : Application() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "paluwagan_notifications"
        private var instance: PaluwaganApp? = null

        fun getInstance(): PaluwaganApp {
            return instance ?: throw IllegalStateException("Application not initialized")
        }

        fun getContext(): Context {
            return getInstance().applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Firebase
        initializeFirebase()

        // Create notification channels
        createNotificationChannels()

        // Set default exception handler
        setupExceptionHandler()
    }

    private fun initializeFirebase() {
        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Configure Firestore settings
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) // Enable offline persistence
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()

        FirebaseFirestore.getInstance().firestoreSettings = settings

        // Enable Firebase Auth persistence
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            auth.currentUser?.let { user ->
                // Update FCM token when user signs in
                updateFCMToken(user.uid)
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create main notification channel
            val name = getString(R.string.app_name)
            val descriptionText = "Notifications for collections and updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Log the exception
            logException(throwable)
            
            // Let the default handler handle it
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun updateFCMToken(userId: String) {
        // This would be implemented when adding FCM for push notifications
        // For now, it's a placeholder for future implementation
    }

    private fun logException(throwable: Throwable) {
        // This would be implemented to log exceptions to a crash reporting service
        // For now, it's a placeholder for future implementation
    }
}
