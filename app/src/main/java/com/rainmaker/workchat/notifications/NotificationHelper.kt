package com.rainmaker.workchat.notifications

import android.annotation.SuppressLint
import android.app.*
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import com.rainmaker.workchat.CHAT_ID
import com.rainmaker.workchat.NOTIFICATION_CHANNEL_ONE_ID
import com.rainmaker.workchat.NOTIFICATION_CHANNEL_ONE_NAME
import com.rainmaker.workchat.R
import com.rainmaker.workchat.activities.MenuActivity

import java.util.Random

/**
 * Created by dmitry on 2/8/18.
 * notification helper
 */

class NotificationHelper(base: Context) : ContextWrapper(base) {

    private var notifManager: NotificationManager? = null
    private val notificationId: Int

    private val manager: NotificationManager
        get() {
            if (notifManager == null) {
                notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            return notifManager as NotificationManager
        }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createChannels() {
        @SuppressLint("WrongConstant") val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ONE_ID,
                NOTIFICATION_CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.setShowBadge(true)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationChannel.importance = NotificationManager.IMPORTANCE_HIGH
        manager.createNotificationChannel(notificationChannel)
    }

    init {
        notificationId = getNotificationId()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun sendOreoNotification(title: String, chatId: String) {
        val intent = Intent(this, MenuActivity::class.java)
        intent.putExtra(CHAT_ID, chatId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = PendingIntent.getActivity(this,
                notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val iconBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val mBuilder = Notification.Builder(applicationContext, NOTIFICATION_CHANNEL_ONE_ID)
                .setContentTitle(title)
                .setLargeIcon(iconBitmap)
                .setSmallIcon(R.drawable.ic_account_circle_black_36dp)
                .setAutoCancel(true)

        val bigText = Notification.BigTextStyle()
        mBuilder.setTicker(title)
        mBuilder.setContentText(title)
        bigText.bigText(title)
        bigText.setBigContentTitle(title)
        mBuilder.setContentText(title)
        mBuilder.setStyle(bigText)
        mBuilder.setContentIntent(contentIntent)
        mBuilder.setStyle(bigText)
        mBuilder.setContentIntent(contentIntent)
        mBuilder.setPriority(Notification.PRIORITY_HIGH)
///        mBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH)
        val uri = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mBuilder.setSound(uri)  //todo

        manager.notify(notificationId, mBuilder.build())
    }

    private fun sendOldNotification(title: String, chatId: String) {
        val mNotificationManager = this
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MenuActivity::class.java)
        intent.putExtra(CHAT_ID, chatId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = PendingIntent.getActivity(this,
                notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val iconBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher) // todo
        val mBuilder = NotificationCompat.Builder(baseContext)
                .setSmallIcon(R.drawable.ic_account_circle_black_36dp) // todo
                .setLargeIcon(iconBitmap)
                .setContentTitle(title)
        val bigText = NotificationCompat.BigTextStyle()
        mBuilder.setTicker(title)
        mBuilder.setContentText(title)
        bigText.bigText(title)
        bigText.setBigContentTitle(title)
        mBuilder.setContentIntent(contentIntent)
        mBuilder.setAutoCancel(true)
        val uri = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mBuilder.setSound(uri)
        mNotificationManager.notify(notificationId, mBuilder.build())
    }

    private fun getNotificationId(): Int {
        val rand = Random()
        val min = 100
        return rand.nextInt(Integer.MAX_VALUE - min + 1) + min
    }

    fun handleNotification(title: String, chatId: String) {
        if (isAppIsInBackground(baseContext)){
            sendNotification(title, chatId)
        } else {
            playNotificationSound()
            // todo notify user, play sound
        }
    }

    private fun sendNotification(title: String, chatId: String) {
        clearNotifications(baseContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendOreoNotification(title, chatId)
        } else {
            sendOldNotification(title, chatId)
        }
    }

    private fun playNotificationSound() {
        try {
            val alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + baseContext.packageName + "/raw/notification")
            val r = RingtoneManager.getRingtone(baseContext, alarmSound)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = am.runningAppProcesses
        runningProcesses
                .filter { it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
                .forEach { processInfo ->
                    processInfo.pkgList
                            .filter { it == context.packageName }
                            .forEach { isInBackground = false }
                }
        return isInBackground
    }

    private fun clearNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

}
