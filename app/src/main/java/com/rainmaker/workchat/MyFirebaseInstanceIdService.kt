
package com.rainmaker.workchat

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class MyFirebaseInstanceIdService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        super.onTokenRefresh()
        val instanceId = FirebaseInstanceId.getInstance().token
        Log.d(TAG, "onTokenRefresh: " + instanceId)
    }

}
