package com.rainmaker.workchat

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.mikepenz.materialdrawer.Drawer
import com.rainmaker.workchat.controllers.ChatRoomsController
import com.rainmaker.workchat.controllers.HomeController
import kotlinx.android.synthetic.main.activity_menu.*
import kotlin.reflect.KClass

class MenuActivity : AppCompatActivity() {

    private lateinit var drawerResult: Drawer
    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        initViews(savedInstanceState)
    }

    private fun initViews(savedInstanceState: Bundle?) {
        toolbarMenu.setTitle(R.string.menu_title)

        val container = findViewById<ViewGroup>(R.id.frameLayout)
        router = Conductor.attachRouter(this, container, savedInstanceState)
        if (!router.hasRootController()){
            router.setRoot(RouterTransaction.with(HomeController()))
        }

        drawerResult = drawer {
            toolbar = toolbarMenu
            showOnFirstLaunch = true

            accountHeader {
                profile("Name", "email@gmail.com") {
                }
            }
            primaryItem(getString(R.string.public_chats)){
                onClick(pushController(ChatRoomsController()))
            }
            primaryItem(getString(R.string.private_chats)){
                onClick(openActivity(ChatListActivity::class))
            }
            primaryItem(getString(R.string.create_chats)){
                onClick(openActivity(NewChatActivity::class))
            }
            primaryItem(getString(R.string.settings)){
                onClick(doSmth())
            }
            primaryItem(getString(R.string.about)){
                onClick(doSmth())
            }
            footer {
                primaryItem(getString(R.string.sign_in))
                divider()
                primaryItem(getString(R.string.sign_up))
            }
        }
    }

    private fun doSmth(): (View?) -> Boolean = {
        false
    }

    private fun <T : Activity> openActivity(activity: KClass<T>): (View?) -> Boolean = {
        startActivity(Intent(this@MenuActivity, activity.java))
        false
    }

    private fun pushController(controller: Controller): (View?) -> Boolean = {
        router.pushController(RouterTransaction.with(controller))
        false
    }

    override fun onBackPressed() {
        if (drawerResult.isDrawerOpen)
            drawerResult.closeDrawer()
        else
            super.onBackPressed()
    }
}
