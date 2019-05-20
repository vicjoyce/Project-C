package com.brianku.qbchat.main_section


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast

import com.brianku.qbchat.R
import com.brianku.qbchat.chat_and_video_calling.ChatAndVideoCallingActivity
import com.brianku.qbchat.groupie_item.HistoryDialogItem
import com.brianku.qbchat.groupie_item.OnlineUserItem
import com.quickblox.auth.session.QBSettings
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.LogLevel
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder


class InboxFragment : Fragment() {

    companion object {
        var currentUser:QBUser? = null
    }

    private var onlineUserGroupAdapter: GroupAdapter<ViewHolder>? = null
    private  var historyDialogGroupAdapter: GroupAdapter<ViewHolder>? = null
    private lateinit var inboxProgressBar: ProgressBar

    private val qbChatService: QBChatService = QBChatService.getInstance()

    private val isLoggedIn: Boolean
        get() = QBChatService.getInstance().isLoggedIn

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val qbUser= arguments?.getSerializable("user") as? QBUser
        qbUser?.let{
            currentUser = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onlineUserGroupAdapter = GroupAdapter<ViewHolder>()
        historyDialogGroupAdapter = GroupAdapter<ViewHolder>()
        initChat()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_inbox_list, container, false)
        inboxProgressBar = view.findViewById<ProgressBar>(R.id.main_progressBar)
        progressBar(false)
        val onlineRecyclerView = view.findViewById<RecyclerView>(R.id.online_user_recyclerview)
        val historyRecyclerView = view.findViewById<RecyclerView>(R.id.history_user_recyclerview)

        onlineUserGroupAdapter?.setOnItemClickListener {
                item, view ->

                var user = (item as OnlineUserItem).item
                var intent = Intent(activity,ChatAndVideoCallingActivity::class.java)
                intent.putExtra("user",user)
                startActivity(intent)
                clearGroupAdapter()
        }

        historyDialogGroupAdapter?.setOnItemClickListener {
                item, view ->

                var dialog = ( item as HistoryDialogItem).item
                var intent = Intent(activity,ChatAndVideoCallingActivity::class.java)
                intent.putExtra("dialog",dialog)
                startActivity(intent)
                clearGroupAdapter()
        }

        onlineRecyclerView.adapter = onlineUserGroupAdapter
        historyRecyclerView.adapter = historyDialogGroupAdapter
        return view
    }

    override fun onStart() {
        super.onStart()

        // reconnect and reload when get back from interrupt
          currentUser?.let{
              loginToChat(it)
          }
        }

    private fun initChat() {
        // initialize Chat Server
        QBSettings.getInstance().logLevel = LogLevel.DEBUG
        QBChatService.setDebugEnabled(true)
        QBChatService.setConfigurationBuilder(QBChatService.ConfigurationBuilder().apply { socketTimeout = 60 })
    }

    private fun loginToChat(user: QBUser) {

        // connect to QBchart server if not login yet, otherwise load whole chatDialogs and users
        if (!isLoggedIn) {
            progressBar(true)
            qbChatService.login(user, object : QBEntityCallback<Void> {
                override fun onSuccess(void: Void?, bundle: Bundle?) {
                    progressBar(false)
                    Toast.makeText(activity, "login to chat service", Toast.LENGTH_SHORT).show()
                    loadChatDialogs()

                }
                override fun onError(e: QBResponseException) {
                    progressBar(false)
                    Toast.makeText(activity, "login to chat service failed ${e.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }else{
            loadChatDialogs()
        }
    }

    private fun loadChatDialogs() {
        // requestBuilder to set parameter while fetching dialogs
        val requestBuilder = QBRequestGetBuilder()
        requestBuilder.limit = 100

        progressBar(true)
        QBRestChatService.getChatDialogs(null,requestBuilder).performAsync(object :QBEntityCallback<ArrayList<QBChatDialog>>{
            override fun onSuccess(dialogs: ArrayList<QBChatDialog>?, p1: Bundle?) {
                dialogs?.forEach { historyDialogGroupAdapter?.add(HistoryDialogItem(it)) }
                // after fetching dialogs, we start to fetching users
                loadUsers()
            }
            override fun onError(e: QBResponseException?) {
                progressBar(false)
                Log.d("vic huang","${e?.message}")
            }
        })
    }

    // fetch whole users from server
    private fun loadUsers(){
            QBUsers.getUsers(null).performAsync(object : QBEntityCallback<ArrayList<QBUser>> {
                override fun onSuccess(qbUsers: ArrayList<QBUser>, p1: Bundle?) {
                   progressBar(false)
                   qbUsers.forEach{
                       if (it.id != currentUser?.id){
                           onlineUserGroupAdapter?.add(OnlineUserItem(it))
                       }}
                }

                override fun onError(e: QBResponseException) {
                    progressBar(false)
                    Toast.makeText(activity, "get users data failed ${e.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }


    private fun progressBar(spin:Boolean){
        if(spin){
            inboxProgressBar.visibility = ProgressBar.VISIBLE
        }else{
            inboxProgressBar.visibility = ProgressBar.INVISIBLE
        }
    }

    private fun clearGroupAdapter(){
        onlineUserGroupAdapter = null
        historyDialogGroupAdapter = null
    }

}


