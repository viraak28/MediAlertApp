package com.medialert.medinotiapp.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.medialert.medinotiapp.MainActivity
import com.medialert.medinotiapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (isStopped) return Result.failure()

        return try {
            withContext(Dispatchers.IO) {
                sendReminder()
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendReminder() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "recordatorio_channel",
                "Recordatorio",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val tipoComida = inputData.getString("tipoComida") ?: return
        val userName = inputData.getString("userName") ?: return

        val builder = NotificationCompat.Builder(context, "recordatorio_channel")
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_notifications_black_24dp))
            .setContentTitle("Recordatorio")
            .setContentText("$userName, es hora de tomar su medicamento de $tipoComida")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(pendingIntent)

        val notificationManagerCompat = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManagerCompat.notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
