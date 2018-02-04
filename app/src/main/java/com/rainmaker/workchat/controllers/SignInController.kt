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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.rainmaker.workchat.App

import com.rainmaker.workchat.R
import com.rainmaker.workchat.User
import com.rainmaker.workchat.activities.MenuActivityInterface
import javax.inject.Inject

/**
 * Created by dmitry on 1/29/18.
 */
class SignInController : Controller(), GoogleApiClient.OnConnectionFailedListener {

    @Inject
    lateinit var mGoogleApiClient: GoogleApiClient

    @Inject
    lateinit var app: App

    private val RC_SIGN_IN = 9001

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var listener: MenuActivityInterface
    private lateinit var mView: View
    private val TAG = "SignInController"

    override fun onContextAvailable(context: Context) {
        super.onContextAvailable(context)
        listener = context as MenuActivityInterface
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        mView = inflater.inflate(R.layout.controller_sign_in, container, false)
        val signInWithGoogle = mView.findViewById<Button>(R.id.sign_in_with_google)
        (mView.context.applicationContext as App).component.inject(this@SignInController)
        signInWithGoogle?.setOnClickListener({signIn()})
        mFirebaseAuth = FirebaseAuth.getInstance()
        return mView
    }

    private fun signIn() {
        mGoogleApiClient.connect()
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(app, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // Google Sign In was successful, authenticate with Firebase
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                // Google Sign In failed
                Log.e(TAG, "Google Sign In failed.")
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(mView.context as AppCompatActivity, OnCompleteListener<AuthResult> { task ->
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful)

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful) {
                        listener.onSignedIn(false)
                    } else {
                        pushUserToDb(mFirebaseAuth.currentUser?.uid, mFirebaseAuth.currentUser?.email, mFirebaseAuth.currentUser?.displayName, mFirebaseAuth.currentUser?.providerId)
                        listener.onSignedIn(true)
                    }
                })
    }

    private fun pushUserToDb(uuid: String?, email: String?, displayName: String?, providerId: String?) {
        val user = User()
        user.email = email
        user.name = displayName
        user.uuid = uuid
        user.provider = providerId
        val mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
        mFirebaseDatabaseReference.child(uuid).setValue(user)
    }

}