package com.monext.sdk.internal.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat

class PaymentForegroundService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP  = "ACTION_STOP"

        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID      = "payment_channel"

        // Helper pour démarrer le service depuis n'importe quel contexte
        fun start(context: Context) {
            val intent = Intent(context, PaymentForegroundService::class.java)
                .apply { action = ACTION_START }
            ContextCompat.startForegroundService(context, intent)
        }

        // Helper pour arrêter le service
        fun stop(context: Context) {
            val intent = Intent(context, PaymentForegroundService::class.java)
                .apply { action = ACTION_STOP }
            context.startService(intent)
        }
    }

    // Evite les appels multiples à makeForeground()
    private var isStarted = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PaymentFGS", "onStartCommand called with action: ${intent?.action}")
        when (intent?.action) {
            ACTION_START -> {
                if (!isStarted) {
                    makeForeground()
                    isStarted = true
                }
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        // START_NOT_STICKY : ne pas redémarrer automatiquement si le process est tué
        return START_NOT_STICKY
    }

    private fun makeForeground() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Paiement en cours")
            .setContentText("Veuillez finaliser votre authentification bancaire")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // ServiceCompat gère automatiquement les différences de versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Paiement",
            NotificationManager.IMPORTANCE_LOW  // Pas de son, pas de popup
        ).apply {
            description = "Maintient le paiement actif pendant l'authentification 3DS"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        isStarted = false
    }

    // Pas de binding nécessaire pour ce cas d'usage
    override fun onBind(intent: Intent?): IBinder? = null
}