package com.rainmaker.workchat.di

import android.support.v4.app.FragmentActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.rainmaker.workchat.App
import com.rainmaker.workchat.R
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by dmitry on 1/30/18.
 */
@Module
class AppModule(val app: App) {
    @Provides
    @Singleton
    fun provideApp() = app

    @Provides
    @Singleton
    fun provideGoogleApiClient() : GoogleApiClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(app.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        return GoogleApiClient.Builder(app)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }
}