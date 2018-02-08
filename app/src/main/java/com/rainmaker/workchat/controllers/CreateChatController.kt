package com.rainmaker.workchat.controllers

import android.support.design.widget.TextInputLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.rainmaker.workchat.App
import com.rainmaker.workchat.R
import com.rainmaker.workchat.presenters.FirebasePresenter
import javax.inject.Inject

/**
 * Created by dmitry on 1/29/18.
 * controller for chat creation
 */
class CreateChatController : Controller() {

    @Inject
    lateinit var presenter: FirebasePresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val mView = inflater.inflate(R.layout.controller_create_chat, container, false)
        (activity?.applicationContext as App).component.inject(this@CreateChatController)
        val createChatButton = mView.findViewById<Button>(R.id.buttonCreateChat)
        val chatNameEditText = mView.findViewById<TextInputLayout>(R.id.textInputLayoutChatName)
        createChatButton.setOnClickListener {
            presenter.createChat(chatNameEditText.editText?.text.toString())
            router.pushController(RouterTransaction.with(PrivateChatRoomsController()))
        }

        return mView
    }

}