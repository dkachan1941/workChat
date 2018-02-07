package com.rainmaker.workchat.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.google.android.gms.appinvite.AppInviteInvitation
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.rainmaker.workchat.*
import com.rainmaker.workchat.activities.ChatActivity.Companion.ROOMS_CHILD
//import com.rainmaker.workchat.activities.ChatActivity.ANONYMOUS
import com.rainmaker.workchat.controllers.*
import kotlinx.android.synthetic.main.activity_menu.*
import javax.inject.Inject
import kotlin.reflect.KClass

class MenuActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, MenuActivityInterface{

    @Inject
    lateinit var mGoogleApiClient: GoogleApiClient
    private val ANONYMOUS = "anonymous"
    lateinit var drawerResult: Drawer
    private lateinit var router: Router
    private lateinit var mFirebaseAuth: FirebaseAuth
    private var mUsername: String? = null
    private var mPhotoUrl: String? = null
    private lateinit var profileImageView: ImageView
    private lateinit var profileNameTextView: TextView
    private val REQUEST_INVITE = 1001
    private val TAG = "MenuActivity"

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

        if (mFirebaseAuth.currentUser == null) {
            router.pushController(RouterTransaction.with(SignInController()))
            return
        } else { // TODO think about !! below
            mUsername = mFirebaseAuth.currentUser!!.displayName
            if (mFirebaseAuth.currentUser!!.photoUrl != null) {
                mPhotoUrl = mFirebaseAuth.currentUser!!.photoUrl!!.toString()
            }
        }
    }

    private fun initViews(savedInstanceState: Bundle?) {
        toolbarMenu.setTitle(R.string.menu_title)

        drawerResult = drawer {
            val displayMetrics = applicationContext.resources.displayMetrics
            toolbar = toolbarMenu
            showOnFirstLaunch = true
            hasStableIds = true
            widthDp = (displayMetrics.widthPixels / displayMetrics.density / 1.5).toInt()
            showOnFirstLaunch = true
            innerShadow = true
            headerViewRes = R.layout.menu_header

            primaryItem(getString(R.string.home)){
                identifier = 100
                icon = R.drawable.abc_ic_star_black_48dp
                onClick(pushController(HomeController::class))
            }

            primaryItem(getString(R.string.public_chats)){
                identifier = 101
                icon = R.drawable.abc_ic_star_black_48dp
                onClick(pushController(PublicChatRoomsController::class))
            }

            primaryItem(getString(R.string.private_chats)){
                identifier = 102
                icon = R.drawable.abc_ic_star_black_48dp
                onClick(pushController(ChatRoomsController::class))
            }

            divider()

            primaryItem(getString(R.string.create_chats)){
                identifier = 103
                icon = R.drawable.abc_ic_star_black_48dp
                onClick(pushController(CreateChatController::class))
            }
            primaryItem(getString(R.string.settings)){
                identifier = 104
                icon = R.drawable.abc_ic_star_black_48dp
                onClick(pushController(SettingsController::class))
            }

            divider()

            primaryItem(getString(R.string.about)){
                identifier = 105
                icon = R.drawable.abc_ic_star_black_48dp
                onClick(pushController(AboutController::class))
            }

            divider()

            primaryItem(getString(R.string.invite_friend)){
                identifier = 106
                icon = R.drawable.abc_ic_star_black_48dp
                onClick(sendInvitation())
            }

            divider()

        }

        with(drawerResult){
            if (mFirebaseAuth.currentUser == null){
//                getDrawerItem(101).withEnabled(false)
                getDrawerItem(102).withEnabled(false)
                getDrawerItem(103).withEnabled(false)
                addStickyFooterItem(createSignInDrawerItem())
            } else {
//                getDrawerItem(101).withEnabled(true)
                getDrawerItem(102).withEnabled(true)
                getDrawerItem(103).withEnabled(true)
                addStickyFooterItem(createSignOutDrawerItem())
            }
        }

        drawerResult.openDrawer()

        profileImageView = drawerResult.header.findViewById(R.id.imageViewProfile)
        profileNameTextView = drawerResult.header.findViewById(R.id.profile_name)

        updateProfileInfo(profileImageView, profileNameTextView)

    }

    private fun sendInvitation(): (View?) -> Boolean = {
//        val intent = AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
//                .setMessage(getString(R.string.invitation_message))
//                .setCallToActionText(getString(R.string.invitation_cta))
//                .build()
//        startActivityForResult(intent, REQUEST_INVITE)
        val intent = AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
            .setMessage(getString(R.string.invitation_message))
//            .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
//            .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
            .setCallToActionText(getString(R.string.invitation_cta))
            .build()
    startActivityForResult(intent, REQUEST_INVITE)
        false
    }

    private fun updateProfileInfo(profileImageView: ImageView, profileNameTextView: TextView) {
        Glide.with(baseContext)
                .load(mFirebaseAuth.currentUser?.photoUrl)
                .asBitmap() // to make image circular
                .centerCrop()
                .placeholder(R.drawable.avatar_anonymous)
                .into(object : BitmapImageViewTarget(profileImageView) {
                    override fun setResource(resource: Bitmap) {
                        val drawable = RoundedBitmapDrawableFactory.create(resources,
                                Bitmap.createScaledBitmap(resource, 100, 100, false))
                        drawable.isCircular = true
                        profileImageView.setImageDrawable(drawable)
                    }
                })

        if (mFirebaseAuth.currentUser?.displayName == null){
            profileNameTextView.text = ""
            profileNameTextView.visibility = View.GONE
        } else {
            profileNameTextView.visibility = View.VISIBLE
            profileNameTextView.text = mFirebaseAuth.currentUser?.displayName
        }
    }

    private fun createSignOutDrawerItem(): IDrawerItem<*, *> {
        return PrimaryDrawerItem()
                .withName(R.string.sign_out)
                .withOnDrawerItemClickListener({ _, _, _ ->
                    signOut()
                    with(drawerResult){
                        removeStickyFooterItemAtPosition(0)
//                        getDrawerItem(101).withEnabled(false)
                        getDrawerItem(102).withEnabled(false)
                        getDrawerItem(103).withEnabled(false)
                        addStickyFooterItem(createSignInDrawerItem())
                    }
                    updateProfileInfo(profileImageView, profileNameTextView)
                    router.setRoot(RouterTransaction.with(HomeController()))
                    false
                })
    }

    private fun createSignInDrawerItem(): IDrawerItem<*, *> {
        return PrimaryDrawerItem()
                .withName(R.string.sign_in)
                .withOnDrawerItemClickListener({ _, _, _ ->
                    with(drawerResult){
                        router.setRoot(RouterTransaction.with(SignInController()))
                    }
                    false
                })
    }

    private fun signOut() {
        mFirebaseAuth.signOut()
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
        mUsername = ANONYMOUS
        mPhotoUrl = null
    }

    private fun <T : Activity> openActivity(activity: KClass<T>): (View?) -> Boolean = {
        startActivity(Intent(this@MenuActivity, activity.java))
        false
    }

    private fun <T : Controller> pushController(controller: KClass<T>): (View?) -> Boolean = {
        router.setRoot(RouterTransaction.with(controller.java.newInstance()).
                pushChangeHandler(HorizontalChangeHandler()).
                popChangeHandler(HorizontalChangeHandler())
        )
        false
    }

    override fun onBackPressed() {
        if (drawerResult.isDrawerOpen)
            drawerResult.closeDrawer()
        else
            if (!router.handleBack()) {
                super.onBackPressed()
            }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show()
    }

    override fun onSignedIn(success: Boolean) {
        if (success){
            router.setRoot(RouterTransaction.with(HomeController()))
            with(drawerResult){
//                getDrawerItem(101).withEnabled(true)
                getDrawerItem(102).withEnabled(true)
                getDrawerItem(103).withEnabled(true)
                removeStickyFooterItemAtPosition(0)
                addStickyFooterItem(createSignOutDrawerItem())
            }
        } else {
            // TODO implement fail
            Toast.makeText(applicationContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
        }
        updateProfileInfo(profileImageView, profileNameTextView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_INVITE) {
            if (resultCode == Activity.RESULT_OK) {
                val ids = AppInviteInvitation.getInvitationIds(resultCode, data!!)
                Log.d(TAG, "Invitations sent: " + ids.size)
            } else {
                Log.d(TAG, "Failed to send invitation.")
            }
        }
    }

}
