
package com.rainmaker.workchat.notifications

import android.os.Bundle
import android.util.Log
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rainmaker.workchat.CHAT_ID
import com.rainmaker.workchat.NOTIFICATION_JOB_TAG
import com.rainmaker.workchat.NOTIFICATION_TITLE
import com.rainmaker.workchat.TAG

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        Log.d(TAG, "Notification recieved from: " + remoteMessage!!.from!!)

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            scheduleJob(remoteMessage.data["title"] ?: "", remoteMessage.data["chatId"] ?: "")
        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification?.body)
        }
    }

    private fun scheduleJob(title: String, chatId: String) {
        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
        val bundle = Bundle()
        bundle.putString(NOTIFICATION_TITLE, title)
        bundle.putString(CHAT_ID, chatId)
        val myJob = dispatcher.newJobBuilder()
                .setService(NotificationJobService::class.java)
                .setTag(NOTIFICATION_JOB_TAG)
                .setExtras(bundle)
                .build()
        dispatcher.schedule(myJob)
    }

}
