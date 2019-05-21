package com.brianku.qbchat.main_section


import android.content.Context
import android.content.Intent
import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

import com.brianku.qbchat.R
import com.brianku.qbchat.chat_and_video_calling.ChatRoomActivity
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
        var opponents:ArrayList<QBUser>? = null
    }

    private var onlineUserGroupAdapter: GroupAdapter<ViewHolder>? = null
    private  var historyDialogGroupAdapter: GroupAdapter<ViewHolder>? = null
    private lateinit var inboxProgressBar: ProgressBar
    private val qbChatService: QBChatService = QBChatService.getInstance()


    private val isLoggedIn: Boolean
        get() = QBChatService.getInstance().isLoggedIn

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // get QBUser from container and save it to Inbox current user
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
        registerUserItemListenerForAdapter()
        registerDialogItemListenerForAdapter()
        onlineRecyclerView.adapter = onlineUserGroupAdapter
        historyRecyclerView.adapter = historyDialogGroupAdapter
        return view
    }

    override fun onStart() {
        super.onStart()

        // connect/reconnect and reload when get back from scratch and interrupt
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

        // connect to QBchat server if not login yet, otherwise load whole chatDialogs and users
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
                    // save all users to opponents
                    opponents = qbUsers
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

    private fun registerUserItemListenerForAdapter(){
        onlineUserGroupAdapter?.setOnItemClickListener {
                item, view ->

            val user = (item as OnlineUserItem).item
            val intent = Intent(activity,ChatRoomActivity::class.java)
            intent.putExtra("user",user)
            intent.putExtra("title",user.fullName)
            startActivity(intent)
            clearGroupAdapter()
        }
    }

    private fun registerDialogItemListenerForAdapter(){
        historyDialogGroupAdapter?.setOnItemClickListener {
                item, view ->

            val dialog = ( item as HistoryDialogItem).item
            val intent = Intent(activity,ChatRoomActivity::class.java)
            val opponentFromDialog = opponents?.filter { dialog.recipientId == it.id }!![0]
            intent.putExtra("dialog",dialog)
            intent.putExtra("opponentFromDialog",opponentFromDialog)
            intent.putExtra("title",dialog.name)
            startActivity(intent)
            clearGroupAdapter()
        }
    }

    private fun clearGroupAdapter(){
        onlineUserGroupAdapter = null
        historyDialogGroupAdapter = null
    }

}


