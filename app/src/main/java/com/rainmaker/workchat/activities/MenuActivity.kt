package com.rainmaker.workchat.activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badgeable.PrimaryDrawerItemKt
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.util.DrawerItemViewHelper
import com.rainmaker.workchat.*
import com.rainmaker.workchat.ChatActivity.ANONYMOUS
import com.rainmaker.workchat.controllers.ChatRoomsController
import com.rainmaker.workchat.controllers.HomeController
import com.rainmaker.workchat.controllers.SignInController
import kotlinx.android.synthetic.main.activity_menu.*
import javax.inject.Inject
import kotlin.reflect.KClass

class MenuActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener{


    @Inject
    lateinit var mGoogleApiClient: GoogleApiClient

    private lateinit var drawerResult: Drawer
    private lateinit var router: Router
    private lateinit var mFirebaseAuth: FirebaseAuth
    private var mFirebaseUser: FirebaseUser? = null
    private var mUsername: String? = null
    private var mPhotoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        (application as App).component.inject(this@MenuActivity)
        mGoogleApiClient.connect()
        val container = findViewById<ViewGroup>(R.id.frameLayout)

        router = Conductor.attachRouter(this, container, savedInstanceState)
        if (!router.hasRootController()){
            router.setRoot(RouterTransaction.with(HomeController()))
        }

        checkAuth()
        initViews(savedInstanceState)
    }

    private fun checkAuth() {
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth.currentUser

        if (mFirebaseUser == null) {
            router.pushController(RouterTransaction.with(SignInController()))
            // Not signed in, launch the Sign In activity
//            startActivity(Intent(this, SignInActivity::class.java))
//            finish()
            return
        } else { // TODO think about !! below
            mUsername = mFirebaseUser!!.displayName
            if (mFirebaseUser!!.photoUrl != null) {
                mPhotoUrl = mFirebaseUser!!.photoUrl!!.toString()
            }
        }
    }

    private fun initViews(savedInstanceState: Bundle?) {
        toolbarMenu.setTitle(R.string.menu_title)

        drawerResult = drawer {
            toolbar = toolbarMenu
            showOnFirstLaunch = true
            hasStableIds = true
            val displayMetrics = applicationContext.resources.displayMetrics
            widthDp = (displayMetrics.widthPixels / displayMetrics.density / 1.5).toInt()
            showOnFirstLaunch = true
            innerShadow = true

            accountHeader {
                compactStyle = true
                profile{
                    name = "Name"
                    email = "Email"
                    nameShown = true
                    this.onClick({ _, _, _ -> false })
                }
            }
            primaryItem(getString(R.string.public_chats)){
                identifier = 101
                icon = R.drawable.abc_ic_star_black_48dp
                onClick(pushController(ChatRoomsController()))
            }
            primaryItem(getString(R.string.private_chats)){
                identifier = 102
                icon = R.drawable.abc_ic_star_black_48dp
                onClick(openActivity(ChatListActivity::class))
            }

            divider()

            primaryItem(getString(R.string.create_chats)){
                identifier = 103
                icon = R.drawable.abc_ic_star_black_48dp
                onClick(openActivity(NewChatActivity::class))
            }
            primaryItem(getString(R.string.settings)){
                identifier = 104
                icon = R.drawable.abc_ic_star_black_48dp
//                onClick(doSmth())
            }

            divider()

            primaryItem(getString(R.string.about)){
                identifier = 105
                icon = R.drawable.abc_ic_star_black_48dp
//                onClick(doSmth())
            }

            divider()

        }

        if (mFirebaseAuth != null){
            drawerResult.addStickyFooterItem(createSignOutDrawerItem())
        }

        drawerResult.openDrawer()
    }

    private fun createSignOutDrawerItem(): IDrawerItem<*, *> {
        return PrimaryDrawerItem()
                .withName(R.string.sign_out)
                .withOnDrawerItemClickListener({ _, _, _ ->
                    signOut()
                    with(drawerResult){
                        removeStickyFooterItemAtPosition(0)
                        getDrawerItem(102).withEnabled(false)
                        getDrawerItem(103).withEnabled(false)
                        addStickyFooterItem(createSignInDrawerItem())
                    }
                    router.pushController(RouterTransaction.with(HomeController()))
                    false
                })
    }

    private fun createSignInDrawerItem(): IDrawerItem<*, *> {
        return PrimaryDrawerItem()
                .withName(R.string.sign_in)
                .withOnDrawerItemClickListener({ _, _, _ ->
                    with(drawerResult){
                        removeStickyFooterItemAtPosition(0)
                        getDrawerItem(102).withEnabled(true)
                        getDrawerItem(103).withEnabled(true)
                        addStickyFooterItem(createSignOutDrawerItem())
                        router.pushController(RouterTransaction.with(SignInController()))
                    }
                    false
                })
    }

    private fun signOut() {
        mFirebaseAuth.signOut()
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
        mFirebaseUser = null
        mUsername = ANONYMOUS
        mPhotoUrl = null
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

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }

}
