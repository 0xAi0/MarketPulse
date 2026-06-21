package com.example.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.data.db.PriceAlert
import com.example.R

object NotificationHelper {
    private const val CHANNEL_ID = "market_pulse_alerts"
    private const val CHANNEL_NAME = "Price Alerts"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText = "Notifications for cryptocurrency price crossings"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showPriceAlertNotification(context: Context, alert: PriceAlert, currentPrice: Double) {
        // Only trigger if permission is granted on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Return gracefully if permission is not granted
                return
            }
        }

        val direction = if (alert.isAbove) "crossed above" else "crossed below"
        val title = "Market Pulse Alert triggered!"
        val content = "${alert.symbol} on ${alert.exchange.uppercase()} has $direction target $${alert.targetPrice} (Currently: $${currentPrice})"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat) // Standard platform asset
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(alert.id, builder.build())
            }
        } catch (e: SecurityException) {
            // Permission wasn't available
        }
    }
}
