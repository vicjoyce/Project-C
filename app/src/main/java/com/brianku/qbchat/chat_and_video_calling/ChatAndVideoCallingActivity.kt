package com.brianku.qbchat.chat_and_video_calling

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.brianku.qbchat.R
import com.brianku.qbchat.extension.hideKeyboard
import com.brianku.qbchat.groupie_item.ChatFromItem
import com.brianku.qbchat.groupie_item.ChatToItem
import com.brianku.qbchat.landing_screen.user_login.user_login.LoginScreenActivity
import com.brianku.qbchat.main_section.InboxFragment
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBIncomingMessagesManager
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.model.QBDialogType
import com.quickblox.chat.request.QBMessageGetBuilder
import com.quickblox.chat.utils.DialogUtils
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_and_video_calling.*
import org.jivesoftware.smack.SmackException
import java.util.zip.Inflater

class ChatAndVideoCallingActivity : AppCompatActivity() {

    private var oppositeUser :QBUser? = null
    private var existDialog:QBChatDialog? = null
    private var messageAdapter:GroupAdapter<ViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_and_video_calling)
        oppositeUser = intent.getSerializableExtra("user") as? QBUser
        existDialog = intent.getSerializableExtra("dialog") as? QBChatDialog
        messageAdapter = GroupAdapter<ViewHolder>()
        chat_message_recyclerview.adapter = messageAdapter

        initChatDialogs()
        fetchMessage()
        initComponent()
    }

    private fun initChatDialogs(){
        // if connect to new user, create a dialog, otherwise use the exist one.
        if(oppositeUser != null){
            createChatDialog()
        }else {
            existDialogForChat(existDialog!!)
        }
    }

    private fun createChatDialog(){
        val dialog = DialogUtils.buildPrivateDialog(oppositeUser!!.id)
        QBRestChatService.createChatDialog(dialog).performAsync(object :QBEntityCallback<QBChatDialog>{
            override fun onSuccess(chatDialog: QBChatDialog?, p1: Bundle?) {
                Toast.makeText(this@ChatAndVideoCallingActivity,"create dialog successfully",Toast.LENGTH_LONG).show()
                existDialog = chatDialog
                existDialog?.let{
                    existDialogForChat(it)
                }
            }
            override fun onError(p0: QBResponseException?) {
                Toast.makeText(this@ChatAndVideoCallingActivity,"create dialog failed",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun existDialogForChat(chatDialog:QBChatDialog){
        chatDialog.initForChat(QBChatService.getInstance())
        val incomingMessage = QBChatService.getInstance().incomingMessagesManager
        incomingMessage.addDialogMessageListener(object:QBChatDialogMessageListener{
            override fun processMessage(p0: String?, p1: QBChatMessage?, p2: Int?) {

            }
            override fun processError(p0: String?, p1: QBChatException?, p2: QBChatMessage?, p3: Int?) {}
        })

        chatDialog.addMessageListener(object :QBChatDialogMessageListener{
            override fun processMessage(p0: String?, qbChatMessage: QBChatMessage?, p2: Int?) {

            }

            override fun processError(p0: String?, p1: QBChatException?, p2: QBChatMessage?, p3: Int?) {}
        })
    }

    private fun fetchMessage(){
        val messageGetBuilder = QBMessageGetBuilder()
        messageGetBuilder.setLimit(500)

        existDialog?.let{
            QBRestChatService.getDialogMessages(it,messageGetBuilder).performAsync(object :QBEntityCallback<ArrayList<QBChatMessage>>{
                override fun onSuccess(qbChatMessages: ArrayList<QBChatMessage>?, p1: Bundle?) {
                    qbChatMessages?.forEach {
                        if(it.senderId == InboxFragment.currentUser?.id){
                            messageAdapter?.add(ChatToItem(it.body))
                        }else{
                            messageAdapter?.add(ChatFromItem(it.body))
                        }
                    }
                }

                override fun onError(p0: QBResponseException?) {
                }
            })
        }
    }

    private fun initComponent(){
        send_message.setOnClickListener {
            val message = chat_message_et.text.toString().trim()
            if(message.isEmpty()){
                return@setOnClickListener
            }
            chat_message_et.setText("")
            chat_message_et.hideKeyboard()
            val chatMessage = QBChatMessage()
            chatMessage.body = message
            chatMessage.senderId = InboxFragment.currentUser?.id
            chatMessage.setSaveToHistory(true)

            // ChatDialog send message
            try {
                existDialog?.sendMessage(chatMessage)
            }catch (e: SmackException.NotConnectedException){
                e.printStackTrace()
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_video_top_nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}
