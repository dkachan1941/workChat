
package com.rainmaker.workchat.notifications

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.rainmaker.workchat.TAG

class MyFirebaseInstanceIdService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        super.onTokenRefresh()
        val instanceId = FirebaseInstanceId.getInstance().token
        Log.d(TAG, "onTokenRefresh: " + instanceId)
    }

}
