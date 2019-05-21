package com.brianku.qbchat.chat_and_video_calling

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler


import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import com.brianku.qbchat.R
import com.brianku.qbchat.VideoFragments.PreviewCallFragment
import com.brianku.qbchat.VideoFragments.VideoConversationFragment
import com.brianku.qbchat.utils.addFragment
import com.brianku.qbchat.utils.addFragmentWithBackStack
import com.brianku.qbchat.utils.popBackStackFragment
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBWebRTCSignaling

import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.*
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback
import org.webrtc.CameraVideoCapturer
import java.util.*

class VideoCallingActivity : AppCompatActivity(),QBRTCClientSessionCallbacks,QBRTCSessionEventsCallback {


    // first step, let rtcClient(WebRTC client) to add Signaling from the signaling manager and chain a listener to create a signaling at local
    // second, to receive callbacks about current session state to know connection state, video/audio track in both local and remote and peer connection states

    private lateinit var opponent:QBUser
    private var mutipleOpponents:ArrayList<QBUser> = ArrayList<QBUser>()
    private var rtcClient:QBRTCClient? = null
    private var currentSession: QBRTCSession? = null
    private var audioManager: AppRTCAudioManager? = null


    /*
     *    **********************************************8
     *     This is Step One: rtcClient add signaling
     */

    private fun initQBRTCClient() {

        rtcClient = QBRTCClient.getInstance(this)
        QBChatService.getInstance().videoChatWebRTCSignalingManager?.addSignalingManagerListener{
                qbSignaling, createdLocally ->
                if(!createdLocally){
                    rtcClient!!.addSignaling(qbSignaling as QBWebRTCSignaling)
                }
        }
        // Configure
        //
        QBRTCConfig.setMaxOpponentsCount(4)
        setSettingsForMultiCall()
        QBRTCConfig.setDebugEnabled(true)

        // Add activity as callback to RTCClient
        rtcClient!!.addSessionCallbacksListener(this)
        // Start mange QBRTCSessions according to VideoCall parser's callbacks
        rtcClient!!.prepareToProcessCalls()
    }
    // set HW for capable to multi call or minimum setting
    private fun setSettingsForMultiCall() {
        if (mutipleOpponents.size == 2) {
            QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.HD_VIDEO.width)
            QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.HD_VIDEO.height)
        } else {
            //set to minimum settings
            QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.QBGA_VIDEO.width)
            QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.QBGA_VIDEO.height)
            QBRTCMediaConfig.setVideoHWAcceleration(false)
        }
    }
//***********************************************************************************************


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_calling)

        opponent = intent.getSerializableExtra("callingReceiver") as QBUser
        initQBRTCClient()
        checkCameraPermissionAndStart()

    }

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is VideoConversationFragment) {
            fragment.initSession(currentSession)
        }
    }

    //****************************************************************************************************
    // initial activity to loading startup fragment -> preview call fragment
    //****************************************************************************************************

    private fun checkCameraPermissionAndStart() {
        if (!isCallPermissionsGranted()) {
            requestCameraPermission()
        } else {
            initPreviewFragment()
        }
    }

    private fun isCallPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 3)
    }

    private fun initPreviewFragIfNeed() {
        if (supportFragmentManager.findFragmentByTag(PreviewCallFragment::class.java.simpleName) !is PreviewCallFragment) {
            initPreviewFragment()
        } else {
            initPreviewFragDelayed()
        }
    }

    private fun initPreviewFragDelayed() {
        Handler().postDelayed(1000L) { popBackStackFragment(supportFragmentManager) }
    }

    private fun initPreviewFragment() {
        val previewFragment = PreviewCallFragment.newInstance(opponent)
        addFragment(supportFragmentManager, R.id.fragment_container, previewFragment, PreviewCallFragment::class.java.simpleName)
    }

    //****************************************************************************************************
    // initial activity to loading secondary fragment -> video conversation fragment
    //****************************************************************************************************

    private fun initConversationFragment(incoming: Boolean) {
        val conversationFragment = VideoConversationFragment.newInstance(incoming, opponent)
        addFragmentWithBackStack(supportFragmentManager, R.id.fragment_container, conversationFragment, VideoConversationFragment::class.java.simpleName)
    }

    private fun updatePreviewCallButtons(show: Boolean) {
        val previewFrag = supportFragmentManager.findFragmentByTag(PreviewCallFragment::class.java.simpleName) as PreviewCallFragment?
        Log.d("vic", "updatePreviewCallButtons")
        if (previewFrag != null) {
            Log.d("vic", "updateCallButtons")
//            previewFrag.updateCallButtons(show)
        }
    }






















    // Below atleast 8 methods to conform QBRTCSessionEventsCallback && QBRTCClientSessionCallbacks QBRTCSession interfaces


    override fun onReceiveNewSession(p0: QBRTCSession?) {

    }

    override fun onUserNoActions(p0: QBRTCSession?, p1: Int?) {

    }

    override fun onUserNotAnswer(p0: QBRTCSession?, p1: Int?) {

    }

    override fun onCallAcceptByUser(p0: QBRTCSession?, p1: Int?, p2: MutableMap<String, String>?) {

    }

    override fun onCallRejectByUser(p0: QBRTCSession?, p1: Int?, p2: MutableMap<String, String>?) {

    }

    override fun onReceiveHangUpFromUser(p0: QBRTCSession?, p1: Int?, p2: MutableMap<String, String>?) {

    }

    override fun onSessionStartClose(p0: QBRTCSession?) {

    }

    override fun onSessionClosed(p0: QBRTCSession?) {

    }


    // setup AppRTCAudioManager, this module is built by QB

    private fun initAudioManagerIfNeed() {
        if (audioManager == null) {
            audioManager = AppRTCAudioManager.create(this)

            audioManager!!.defaultAudioDevice = AppRTCAudioManager.AudioDevice.SPEAKER_PHONE
            Log.d("vic", "AppRTCAudioManager.AudioDevice.SPEAKER_PHONE")

            audioManager!!.setOnWiredHeadsetStateListener { plugged, hasMicrophone ->
                Log.d("vic", "setOnWiredHeadsetStateListener plugged= $plugged")
            }
        }
    }

    private fun startAudioManager() {
        initAudioManagerIfNeed()
        audioManager!!.start { selectedAudioDevice, _ ->
            Toast.makeText(applicationContext, "Audio device switched to  $selectedAudioDevice", Toast.LENGTH_SHORT).show()
            updateAudioDevice()
        }
    }

    private fun updateAudioDevice() {
        val videoFrag = supportFragmentManager.findFragmentByTag(VideoConversationFragment::class.java.simpleName) as VideoConversationFragment?
        Log.d("vic", "updateAudioDevice")
        if (videoFrag != null) {
            videoFrag.audioDeviceChanged(audioManager!!.selectedAudioDevice)
        }
    }

}
