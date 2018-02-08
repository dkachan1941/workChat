package com.rainmaker.workchat.controllers

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.bluelinelabs.conductor.Controller
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.rainmaker.workchat.*

import com.rainmaker.workchat.activities.MenuActivityInterface
import javax.inject.Inject

/**
 * Created by dmitry on 1/29/18.
 * sign in controller
 */
class SignInController : Controller(), GoogleApiClient.OnConnectionFailedListener {

    @Inject
    lateinit var mGoogleApiClient: GoogleApiClient

    @Inject
    lateinit var app: App

    private lateinit var mFireBaseAuth: FirebaseAuth
    private lateinit var listener: MenuActivityInterface
    private lateinit var mView: View

    override fun onContextAvailable(context: Context) {
        super.onContextAvailable(context)
        listener = context as MenuActivityInterface
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        mView = inflater.inflate(R.layout.controller_sign_in, container, false)
        (mView.context.applicationContext as App).component.inject(this@SignInController)
        val signInWithGoogle = mView.findViewById<Button>(R.id.sign_in_with_google)
        signInWithGoogle?.setOnClickListener({signIn()})
        mFireBaseAuth = FirebaseAuth.getInstance()
        return mView
    }

    private fun signIn() {
        mGoogleApiClient.connect()
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(app, app.getString(R.string.err_google_services), Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess && result?.signInAccount != null) {
                fireBaseAuthWithGoogle(result.signInAccount!!)
            } else {
                Log.e(TAG, app.getString(R.string.err_sign_in_google_services))
            }
        }
    }

    private fun fireBaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mFireBaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(mView.context as AppCompatActivity, { task ->
                    listener.onSignedIn(task.isSuccessful)
                })
    }

}