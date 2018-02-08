package com.rainmaker.workchat.di

import com.rainmaker.workchat.App
import com.rainmaker.workchat.activities.MenuActivity
import com.rainmaker.workchat.controllers.CreateChatController
import com.rainmaker.workchat.controllers.SignInController
import com.rainmaker.workchat.presenters.FirebasePresenterImpl
import dagger.Component
import javax.inject.Singleton

/**
 * Created by dmitry on 1/30/18.
 *
 */
@Singleton
@Component(modules = arrayOf(AppModule::class, PresenterModule::class))
interface AppComponent {
    fun inject(app: App)
    fun inject(target: MenuActivity)
    fun inject(target: SignInController)
    fun inject(target: FirebasePresenterImpl)
    fun inject(target: CreateChatController)
}