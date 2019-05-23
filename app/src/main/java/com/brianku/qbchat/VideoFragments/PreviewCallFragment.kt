package com.brianku.qbchat.VideoFragments


import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.*

import android.widget.TextView
import android.widget.Toast
import com.brianku.qbchat.R
import com.brianku.qbchat.view.CameraPreview
import com.quickblox.chat.QBChatService
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCClient
import com.quickblox.videochat.webrtc.QBRTCSession
import com.quickblox.videochat.webrtc.QBRTCTypes
import kotlinx.android.synthetic.main.fragment_preview_call.*
import kotlinx.android.synthetic.main.view_action_buttons_conversation_fragment.*
import org.webrtc.ContextUtils


class PreviewCallFragment : BaseToolBarFragment() {

    private val TAG = "PreviewCallFragment"

    //for future multi call
    private var opponents:ArrayList<QBUser> = ArrayList()

    private lateinit var cameraPreview: CameraPreview
    private lateinit var opponent: QBUser
    private lateinit var eventListener: CallFragmentCallbackListener
    private lateinit var snackBar: com.google.android.material.snackbar.Snackbar
    private var isIncomingCall: Boolean = false

    override val fragmentLayout: Int
        get() = R.layout.fragment_preview_call

    interface CallFragmentCallbackListener {
        fun onStartCall(session: QBRTCSession)
        fun onAcceptCall()
        fun onRejectCall()
        fun onLogout()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            eventListener = context as CallFragmentCallbackListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement CallFragmentCallbackListener")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        initFields()
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isIncomingCall = false
        button_start_call.setOnClickListener{ startOrAcceptCall() }
        startCallButtonVisibility(View.VISIBLE)
        button_hangup_call.setOnClickListener{ rejectCall() }
        hangUpButtonVisibility(View.GONE)
        initSnackBar()

        Toast.makeText(activity,"preview-call is ready",Toast.LENGTH_LONG).show()
    }

    private fun initFields() {
        arguments!!.get("qbUser").let {
            if (it is ArrayList<*>) {
                opponents = it.filterIsInstance<QBUser>() as ArrayList<QBUser>
                val currentUser = QBChatService.getInstance().user
                opponents.remove(currentUser)
            }else{
                opponent = it as QBUser
                opponents.add(opponent)
            }
        }
        Log.d(TAG, "users= $opponent")
    }

    @SuppressLint("NewApi")
    private fun initSnackBar() {
        snackBar = com.google.android.material.snackbar.Snackbar.make(snackbar_view, R.string.text_incoming_video_call, com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE)
        val textView = snackBar.view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
    }


    fun updateCallButtons(show: Boolean) {
        Log.d(TAG, "updateCallButtons show= $show")
        if (show) {
            isIncomingCall = true
            hangUpButtonVisibility(View.VISIBLE)
            snackBar.show()
        } else {
            isIncomingCall = false
            hangUpButtonVisibility(View.GONE)
            snackBar.dismiss()
        }
    }


    // set the red circle imageButton visible/gone, during the call, the buttom should be red, and when over, the button set gone
    private fun hangUpButtonVisibility(visibility: Int) {
        button_hangup_call?.visibility = visibility
    }

    private fun startOrAcceptCall() {
        if (isIncomingCall) {
            isIncomingCall = false
            snackBar.dismiss()
            startCallButtonVisibility(View.GONE)
            eventListener.onAcceptCall()
        } else {
            startCall()
        }
    }

    private fun rejectCall() {
        eventListener.onRejectCall()
        hangUpButtonVisibility(View.GONE)
        snackBar.dismiss()
    }

    private fun startCall() {
        if (opponents.size > 4) {
            Toast.makeText(activity, String.format(getString(R.string.error_max_opponents_count),
                4), Toast.LENGTH_LONG).show()
            return
        }

        val opponentsList = getIdsOpponents(opponents)
        val conferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO

        val qbrtcClient = QBRTCClient.getInstance(ContextUtils.getApplicationContext())
        val newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType)
        eventListener.onStartCall(newQbRtcSession)
    }

    override fun onResume() {
        super.onResume()
        startCameraPreview()
    }

    private fun startCameraPreview() {
        cameraPreview = CameraPreview(activity!!, Camera.CameraInfo.CAMERA_FACING_FRONT)
        camera_preview.addView(cameraPreview)
    }

    private fun stopCameraPreview() {
        cameraPreview.stop()
    }

    override fun onPause() {
        super.onPause()
        stopCameraPreview()
        camera_preview.removeAllViews()
    }


    private fun startCallButtonVisibility(visibility: Int) {
        button_start_call.visibility = visibility
    }


    private fun getIdsOpponents(selectedUsers: Collection<QBUser>): ArrayList<Int> {
        val opponentsIds = ArrayList<Int>()
        if (!selectedUsers.isEmpty()) {
            for (qbUser in selectedUsers) {
                opponentsIds.add(qbUser.id)
            }
        }
        return opponentsIds
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_call, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout_user_done -> {
                eventListener.onLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    companion object {

        fun newInstance(opponent: QBUser)=
            PreviewCallFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("qbUser", opponent)
                }
            }
    }
}
