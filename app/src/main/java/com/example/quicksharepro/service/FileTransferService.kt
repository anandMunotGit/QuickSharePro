package com.example.quicksharepro.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.quicksharepro.MainActivity
import com.example.quicksharepro.domain.model.TransferStatus
import com.example.quicksharepro.domain.repository.FileTransferRepository
import com.example.quicksharepro.ui.screens.filepicker.formatFileSize
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import android.net.wifi.WifiManager
import android.os.PowerManager
import javax.inject.Inject

@AndroidEntryPoint
class FileTransferService : Service() {

    @Inject
    lateinit var repository: FileTransferRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val NOTIFICATION_ID = 101
    private val CHANNEL_ID = "transfer_channel"

    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireLocks()
        observeTransfer()
    }

    private fun acquireLocks() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "QuickSharePro::TransferWakeLock")
            wakeLock?.acquire(30 * 60 * 1000L) // 30 min max

            val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "QuickSharePro::TransferWifiLock")
            wifiLock?.acquire()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseLocks() {
        try {
            wakeLock?.let { if (it.isHeld) it.release() }
            wakeLock = null

            wifiLock?.let { if (it.isHeld) it.release() }
            wifiLock = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeTransfer() {
        serviceScope.launch {
            combine(repository.currentTransfer, repository.transferProgress) { session, progress ->
                Pair(session, progress)
            }.collect { (session, progress) ->
                if (session == null) {
                    stopForeground(true)
                    stopSelf()
                    return@collect
                }

                updateNotification(session.status, progress?.totalBytesTransferred ?: 0L, session.totalSize)
                
                if (session.status == TransferStatus.COMPLETED || 
                    session.status == TransferStatus.FAILED || 
                    session.status == TransferStatus.CANCELLED) {
                    delay(2000)
                    stopForeground(false)
                    stopSelf()
                }
            }
        }
    }

    private fun updateNotification(status: TransferStatus, transferred: Long, total: Long) {
        val progress = if (total > 0) (transferred * 100 / total).toInt() else 0
        val contentText = if (status == TransferStatus.COMPLETED) "Transfer Complete" else "Transferring: $progress%"
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("QuickShare Pro")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(status == TransferStatus.TRANSFERRING)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "File Transfer"
            val descriptionText = "Notifications for active file transfers"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("QuickShare Pro")
            .setContentText("Preparing transfer...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
            
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        releaseLocks()
        serviceScope.cancel()
    }
}
