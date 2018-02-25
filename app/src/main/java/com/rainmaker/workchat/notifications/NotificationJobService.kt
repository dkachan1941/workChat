package com.rainmaker.workchat.notifications

import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.rainmaker.workchat.CHAT_ID
import com.rainmaker.workchat.NOTIFICATION_TITLE

/**
 * Created by dmitry on 2/8/18.
 * handle notifications from background
 */

class NotificationJobService : JobService() {

    override fun onStartJob(job: JobParameters): Boolean {
        val notificationHelper = NotificationHelper(this)
        notificationHelper.handleNotification(job.extras?.getString(NOTIFICATION_TITLE) ?: "", job.extras?.getString(CHAT_ID) ?: "")
        return false
    }

    override fun onStopJob(job: JobParameters): Boolean {
        return false
    }

}