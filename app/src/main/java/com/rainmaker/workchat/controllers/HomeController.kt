package com.rainmaker.workchat.controllers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller

import com.rainmaker.workchat.R

/**
 * Created by dmitry on 1/29/18.
 */
class HomeController: Controller() {
    companion object {
        val TAG_ROUTER = "HomeController"
    }
    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_home, container, false)
    }
}