package com.rainmaker.workchat.di

import com.rainmaker.workchat.presenters.AuthPresenter
import com.rainmaker.workchat.presenters.AuthPresenterImpl
import com.rainmaker.workchat.presenters.FirebasePresenter
import com.rainmaker.workchat.presenters.FirebasePresenterImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by dmitry on 2/8/18.
 */

@Module
class PresenterModule {
    @Provides
    @Singleton
    fun provideAuthPresenter(): AuthPresenter = AuthPresenterImpl()

    @Provides
    @Singleton
    fun provideFirebasePresenter(): FirebasePresenter = FirebasePresenterImpl()

}