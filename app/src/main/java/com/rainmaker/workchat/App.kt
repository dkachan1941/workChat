package com.rainmaker.workchat

import android.app.Application
import com.rainmaker.workchat.di.AppComponent
import com.rainmaker.workchat.di.AppModule
import com.rainmaker.workchat.di.DaggerAppComponent

/**
 * Created by dmitry on 1/30/18.
 *
 */
class App : Application() {

    val component: AppComponent by lazy {
        DaggerAppComponent
                .builder()
                .appModule(AppModule(this))
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        component.inject(this)
    }
}