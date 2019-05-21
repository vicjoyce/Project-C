package com.brianku.qbchat.VideoFragments


import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.brianku.qbchat.R
import com.brianku.qbchat.view.CameraPreview
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.QBRTCSession
import kotlinx.android.synthetic.main.view_action_buttons_conversation_fragment.*


class PreviewCallFragment : BaseToolBarFragment() {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_preview_call, container, false)
    }


    fun updateCallButtons(show: Boolean) {
        Log.d("vic", "updateCallButtons show= $show")
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


    companion object {

        fun newInstance(opponent: QBUser)=
            PreviewCallFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("qbUser", opponent)
                }
            }
    }
}
