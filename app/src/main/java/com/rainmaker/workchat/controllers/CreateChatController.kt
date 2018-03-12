package com.rainmaker.workchat.controllers

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.rainmaker.workchat.App
import com.rainmaker.workchat.R
import com.rainmaker.workchat.modifyEncryptionKey
import com.rainmaker.workchat.presenters.FirebasePresenter
import com.rainmaker.workchat.showAlertDialog
import javax.inject.Inject
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager


/**
 * Created by dmitry on 1/29/18.
 * controller for chat creation
 */
class CreateChatController : Controller() {

    @Inject
    lateinit var presenter: FirebasePresenter

    private lateinit var chatNameEditText: TextInputLayout
    private lateinit var textInputLayoutChatPassword: TextInputLayout
    private lateinit var textInputLayoutChatEncriptionPassword: TextInputLayout
    private lateinit var radioButtonPrivate: RadioButton
    private lateinit var radioButtonPublic: RadioButton
    private lateinit var createChatButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val mView = inflater.inflate(R.layout.controller_create_chat, container, false)
        (activity?.applicationContext as App).component.inject(this@CreateChatController)
        setUpViews(mView)
        return mView
    }

    private fun setUpViews(mView: View) {
        createChatButton = mView.findViewById(R.id.buttonCreateChat)
        chatNameEditText = mView.findViewById(R.id.textInputLayoutChatName)
        textInputLayoutChatPassword = mView.findViewById(R.id.textInputLayoutChatPassword)
        textInputLayoutChatEncriptionPassword = mView.findViewById(R.id.textInputLayoutChatEncriptionPassword)
        radioButtonPrivate = mView.findViewById(R.id.radioButtonPrivate)
        radioButtonPublic = mView.findViewById(R.id.radioButtonPublic)

        createChatButton.setOnClickListener {
            setUpCreateChatButton()
        }

        radioButtonPrivate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                radioButtonPublic.isChecked = false
            }
        }

        radioButtonPublic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                radioButtonPrivate.isChecked = false
            }
        }
    }

    private fun setUpCreateChatButton() {
        if (chatNameEditText.editText?.text?.trim()?.length ?: 0 == 0){
            showAlertDialog(activity!!, "New chat", "Set new chat name")
            return
        }

        val password: String = textInputLayoutChatPassword.editText?.text?.toString() ?: ""
        val isPrivate = radioButtonPrivate.isChecked
        val encryptionPw = modifyEncryptionKey(textInputLayoutChatEncriptionPassword.editText?.text?.toString() ?: "")

        presenter.createChat(chatNameEditText.editText?.text.toString(), password, encryptionPw, isPrivate)

        hideKeyBoard()

        if (isPrivate){
            router.pushController(RouterTransaction.with(PrivateChatRoomsController()))
        } else {
            router.pushController(RouterTransaction.with(PublicChatRoomsController()))
        }
    }

    fun hideKeyBoard(){
        val view = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}