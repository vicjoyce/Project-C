package com.brianku.qbchat.VideoFragments


import android.os.Bundle
import androidx.fragment.app.Fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.brianku.qbchat.R
import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.AppRTCAudioManager
import com.quickblox.videochat.webrtc.QBRTCSession
import org.webrtc.CameraVideoCapturer


class VideoConversationFragment : BaseToolBarFragment() {

    private var currentSession: QBRTCSession? = null

    override val fragmentLayout: Int
        get() = R.layout.fragment_video_conversation

    interface CallFragmentCallbackListener {
        fun onHangUpCall()
        fun onSwitchAudio()
        fun onStartScreenSharing()
        fun onSwitchCamera(cameraSwitchHandler: CameraVideoCapturer.CameraSwitchHandler)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_conversation, container, false)
    }


    fun audioDeviceChanged(newAudioDevice: AppRTCAudioManager.AudioDevice) {
//        toggle_speaker.isChecked = newAudioDevice != AppRTCAudioManager.AudioDevice.SPEAKER_PHONE
    }

    fun initSession(session: QBRTCSession?) {
        currentSession = session
    }


    companion object {

        fun newInstance(incoming: Boolean, opponent: QBUser) =
            VideoConversationFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("conversation_reason", incoming)
                    putSerializable("qbUser", opponent)
                }
            }
    }
}
