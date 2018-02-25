
package com.rainmaker.workchat.notifications

import android.os.Bundle
import android.util.Log
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Trigger
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

//    private var notificationUtils: NotificationUtils? = null

//    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
//        Log.d(TAG, "Notification recieved")
//
//        if (remoteMessage == null) return

        // Check if message contains a notification payload.
//        if (remoteMessage.notification != null) {
//            Log.e(TAG, "Notification Body: " + remoteMessage.notification?.body)
//            handleNotification(remoteMessage.notification?.body ?: "")
//        }

        // Check if message contains a data payload.
//        if (remoteMessage.data.isNotEmpty()) {
//            Log.e(TAG, "Data Payload: " + remoteMessage.data.toString())
//
//            try {
//                val json = JSONObject(remoteMessage.data.toString())
//                handleDataMessage(json)
//            } catch (e: Exception) {
//                Log.e(TAG, "Notification Exception: " + e.message)
//            }
//
//        }
//    }

//    private fun handleNotification(message: String?) {
//        if (!NotificationUtils.isAppIsInBackground(applicationContext)) {
//            // app is in foreground, broadcast the push message
//            val pushNotification = Intent(PUSH_NOTIFICATION)
//            pushNotification.putExtra("message", String.format(resources.getString(R.string.notification_message), message))
//            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)
//
//            // play notification sound
//            val notificationUtils = NotificationUtils(applicationContext)
//            notificationUtils.playNotificationSound()
//        } else {
//            // If the app is in background, firebase itself handles the notification
//        }
//    }
//
//    private fun handleDataMessage(json: JSONObject) {
//        Log.e(TAG, "push json: " + json.toString())
//
//        try {
//            val data = json.getJSONObject("data")
//
//            val title = data.getString("title")
//            val message = data.getString("message")
//            val isBackground = data.getBoolean("is_background")
//            val imageUrl = data.getString("image")
//            val timestamp = data.getString("timestamp")
//            val payload = data.getJSONObject("payload")
//
//            Log.e(TAG, "title: " + title)
//            Log.e(TAG, "message: " + message)
//            Log.e(TAG, "isBackground: " + isBackground)
//            Log.e(TAG, "payload: " + payload.toString())
//            Log.e(TAG, "imageUrl: " + imageUrl)
//            Log.e(TAG, "timestamp: " + timestamp)
//
//            if (!NotificationUtils.isAppIsInBackground(applicationContext)) {
//                // app is in foreground, broadcast the push message
//                val pushNotification = Intent(PUSH_NOTIFICATION)
//                pushNotification.putExtra("message", String.format(resources.getString(R.string.notification_message), message))
//                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)
//
//                // play notification sound
//                val notificationUtils = NotificationUtils(applicationContext)
//                notificationUtils.playNotificationSound()
//            } else {
//                // app is in background, show the notification in notification tray
//                //                Intent resultIntent = new Intent(getApplicationContext(), ChatRoomsActivity.class);
//                //                resultIntent.putExtra("message", String.format(getResources().getString(R.string.notification_message), message));
//
//                // check for image attachment
//                if (TextUtils.isEmpty(imageUrl)) {
//                    //                    showNotificationMessage(getApplicationContext(), title, String.format(getResources().getString(R.string.notification_message), message), timestamp, resultIntent);
//                } else {
//                    // image is present, show notification with image
//                    //                    showNotificationMessageWithBigImage(getApplicationContext(), title, String.format(getResources().getString(R.string.notification_message), message), timestamp, resultIntent, imageUrl);
//                }
//            }
//        } catch (e: JSONException) {
//            Log.e(TAG, "Json Exception: " + e.message)
//        } catch (e: Exception) {
//            Log.e(TAG, "Exception: " + e.message)
//        }
//
//    }
//
//    /**
//     * Showing notification with text only
//     */
//    private fun showNotificationMessage(context: Context, title: String, message: String, timeStamp: String, intent: Intent) {
//        notificationUtils = NotificationUtils(context)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        notificationUtils!!.showNotificationMessage(title, String.format(resources.getString(R.string.notification_message), message), timeStamp, intent)
//    }
//
//    /**
//     * Showing notification with text and image
//     */
//    private fun showNotificationMessageWithBigImage(context: Context, title: String, message: String, timeStamp: String, intent: Intent, imageUrl: String) {
//        notificationUtils = NotificationUtils(context)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        notificationUtils!!.showNotificationMessage(title, String.format(resources.getString(R.string.notification_message), message), timeStamp, intent, imageUrl)
//    }

}
