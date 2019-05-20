package com.brianku.qbchat.chat_and_video_calling

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.RecyclerView
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

    // opponent is 1:1 user we message or video calling to him/her
    private var opponent :QBUser? = null

    // mainDialog is chat-room alike data model, it handles message send and retrieve from QBChat Server, it also could display user is typing status
    private var mainDialog:QBChatDialog? = null

    private var messageAdapter:GroupAdapter<ViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_and_video_calling)

        opponent = intent.getSerializableExtra("user") as? QBUser
        mainDialog = intent.getSerializableExtra("dialog") as? QBChatDialog
        val title = intent.getStringExtra("title")
        supportActionBar?.title = title

        messageAdapter = GroupAdapter<ViewHolder>()
        adapterInsertItemListener()
        chat_message_recyclerview.adapter = messageAdapter

        initChatDialogs()
        fetchMessage()
        initComponent()
    }

    private fun initChatDialogs(){
        // if user select user from Inbox then create a dialog, otherwise we use exist main dialog to handle message.
        if(opponent != null){
            createChatDialog()
        }else {
            initDialogForChat(mainDialog!!)
        }
    }

    private fun createChatDialog(){
        // we use the opponent id to create a new private 1:1 dialog to server is no there is no exist one in QB Chat server
        val dialog = DialogUtils.buildPrivateDialog(opponent!!.id)

        QBRestChatService.createChatDialog(dialog).performAsync(object :QBEntityCallback<QBChatDialog>{
            override fun onSuccess(chatDialog: QBChatDialog?, p1: Bundle?) {
                Toast.makeText(this@ChatAndVideoCallingActivity,"create dialog successfully",Toast.LENGTH_LONG).show()
                mainDialog = chatDialog
                mainDialog?.let{
                    // when main dialog is created, we start to initialize main dialog(chat room)
                    initDialogForChat(it)
                }
            }
            override fun onError(p0: QBResponseException?) {
                Toast.makeText(this@ChatAndVideoCallingActivity,"create dialog failed",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun initDialogForChat(chatDialog:QBChatDialog){
        // dialog initialize for chatting service
        chatDialog.initForChat(QBChatService.getInstance())
        // when main dialog is initialized,retrieve QBInComingMessagesManager from QBChatService
        // and register QBChatDialogMessageListener to listen for messages from chat dialogs
        val incomingMessage = QBChatService.getInstance().incomingMessagesManager
        incomingMessage.addDialogMessageListener(object:QBChatDialogMessageListener{
            override fun processMessage(p0: String?, p1: QBChatMessage?, p2: Int?) {}
            override fun processError(p0: String?, p1: QBChatException?, p2: QBChatMessage?, p3: Int?) {}
        })

        // Add Message listener for chat dialog
        chatDialog.addMessageListener(object :QBChatDialogMessageListener{
            override fun processMessage(p0: String?, qbChatMessage: QBChatMessage?, p2: Int?) {
                qbChatMessage?.let{
                    showMessage(it)
                }
            }
            override fun processError(p0: String?, p1: QBChatException?, p2: QBChatMessage?, p3: Int?) {}
        })
    }

    private fun fetchMessage(){
        val messageGetBuilder = QBMessageGetBuilder()
        messageGetBuilder.setLimit(500)

        mainDialog?.let{
            QBRestChatService.getDialogMessages(it,messageGetBuilder).performAsync(object :QBEntityCallback<ArrayList<QBChatMessage>>{
                override fun onSuccess(qbChatMessages: ArrayList<QBChatMessage>?, p1: Bundle?) {
                    qbChatMessages?.forEach {
                      showMessage(it)
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
                mainDialog?.sendMessage(chatMessage)
                showMessage(chatMessage)
            }catch (e: SmackException.NotConnectedException){
                e.printStackTrace()
            }
        }
    }

    private fun adapterInsertItemListener(){
        messageAdapter!!.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver(){
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    chat_message_recyclerview.layoutManager!!.smoothScrollToPosition(chat_message_recyclerview,null,messageAdapter!!.itemCount)
                }
            }
        )
    }


    private fun showMessage(chatMessage:QBChatMessage){
        if(chatMessage.senderId == InboxFragment.currentUser?.id){
            messageAdapter?.add(ChatToItem(chatMessage.body))
        }else{
            messageAdapter?.add(ChatFromItem(chatMessage.body))
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
