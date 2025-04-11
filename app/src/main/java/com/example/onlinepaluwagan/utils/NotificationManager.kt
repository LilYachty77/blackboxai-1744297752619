package com.example.onlinepaluwagan.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.onlinepaluwagan.R
import com.example.onlinepaluwagan.DashboardActivity

object NotificationManager {
    private const val CHANNEL_ID = "paluwagan_notifications"
    private const val CHANNEL_NAME = "Paluwagan Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for payments and updates"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showPaymentNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int
    ) {
        val intent = Intent(context, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
            as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    fun showUpcomingPaymentNotification(
        context: Context,
        groupName: String,
        amount: Double,
        dueDate: String,
        notificationId: Int
    ) {
        val message = "Payment of ₱$amount for group '$groupName' is due on $dueDate"
        showPaymentNotification(
            context,
            "Upcoming Payment",
            message,
            notificationId
        )
    }

    fun showPaymentReceivedNotification(
        context: Context,
        groupName: String,
        amount: Double,
        memberName: String,
        notificationId: Int
    ) {
        val message = "Received payment of ₱$amount from $memberName in group '$groupName'"
        showPaymentNotification(
            context,
            "Payment Received",
            message,
            notificationId
        )
    }
}
