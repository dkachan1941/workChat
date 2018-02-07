package com.rainmaker.workchat.controllers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller

import com.rainmaker.workchat.R

/**
 * Created by dmitry on 1/29/18.
 */
class SettingsController : Controller() {
    companion object {
        const val TAG_ROUTER = "SettingsController"
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_settings, container, false)
    }
}