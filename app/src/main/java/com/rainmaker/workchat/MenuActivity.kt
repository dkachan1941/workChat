package com.rainmaker.workchat

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import com.mikepenz.materialdrawer.Drawer
import kotlinx.android.synthetic.main.activity_menu.*
import kotlin.reflect.KClass

class MenuActivity : AppCompatActivity() {

    private lateinit var drawerResult: Drawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        initViews()
    }

    private fun initViews() {
        toolbarMenu.setTitle(R.string.menu_title)

        drawerResult = drawer {
            toolbar = toolbarMenu
            showOnFirstLaunch = true

            accountHeader {
                profile("Name", "email@gmail.com") {
                }
            }
            primaryItem(getString(R.string.public_chats)){
                onClick(openActivity(ChatRoomsActivity::class))
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

    override fun onBackPressed() {
        if (drawerResult.isDrawerOpen)
            drawerResult.closeDrawer()
        else
            super.onBackPressed()
    }
}
