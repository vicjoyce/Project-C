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
import com.brianku.qbchat.VideoFragments.ScreenShareFragment
import com.brianku.qbchat.VideoFragments.VideoConversationFragment
import com.brianku.qbchat.utils.addFragment
import com.brianku.qbchat.utils.addFragmentWithBackStack
import com.brianku.qbchat.utils.popBackStackFragment
import com.quickblox.auth.session.QBSettings
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBWebRTCSignaling

import com.quickblox.users.model.QBUser
import com.quickblox.videochat.webrtc.*
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback
import org.webrtc.CameraVideoCapturer
import java.util.*

class VideoCallingActivity : AppCompatActivity(),QBRTCClientSessionCallbacks,QBRTCSessionEventsCallback
,VideoConversationFragment.CallFragmentCallbackListener, PreviewCallFragment.CallFragmentCallbackListener, ScreenShareFragment.OnSharingEvents
{


    // first step, let rtcClient(WebRTC client) to add Signaling from the signaling manager and chain a listener to create a signaling at local
    // second, to receive callbacks about current session state to know connection state, video/audio track in both local and remote and peer connection states

    private val TAG = "VideoCallingActivity"
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
        Log.d(TAG,"Video is complete initialized")

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
        Log.d(TAG, "updatePreviewCallButtons")
        if (previewFrag != null) {
            Log.d(TAG, "updateCallButtons")
            previewFrag.updateCallButtons(show)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            // when PERMISSIONS_FOR_CALL_REQUEST = 3
                3 ->
                if (grantResults.isNotEmpty()) {
                    if (!isCallPermissionsGranted()) {

                        Toast.makeText(applicationContext, getString(R.string.denied_permission_message, Arrays.toString(permissions)), Toast.LENGTH_LONG).show()
                        startLogout()
                        finish()
                    } else {
                        initPreviewFragment()
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "onActivityResult requestCode=$requestCode, resultCode= $resultCode")
        if (requestCode == QBRTCScreenCapturer.REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK) {
                startScreenSharing(data!!)
                Log.i(TAG, "Starting screen capture")
            } else {
                Toast.makeText(applicationContext, getString(R.string.denied_permission_message, "screen"), Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun startScreenSharing(data: Intent) {
        val screenShareFragment = ScreenShareFragment.newInstance()
        addFragmentWithBackStack(supportFragmentManager, R.id.fragment_container, screenShareFragment, ScreenShareFragment::class.java.simpleName)
        currentSession!!.mediaStreamManager.videoCapturer = QBRTCScreenCapturer(data, null)
    }

    private fun startLogout() {
        QBChatService.getInstance().destroy()
    }

    private fun initCurrentSession(session: QBRTCSession) {
        Log.d(TAG, "Init new QBRTCSession addSessionCallbacksListener")
        currentSession = session
    }

    private fun releaseCurrentSession() {
        Log.d(TAG, "Release current session removeSessionCallbacksListener")
        if (currentSession != null) {
            this.currentSession = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rtcClient!!.removeSessionsCallbacksListener(this@VideoCallingActivity)
        rtcClient!!.destroy()
    }

    override fun onBackPressed() {
        val fragmentByTag = supportFragmentManager.findFragmentByTag(ScreenShareFragment::class.java.simpleName)
        if (fragmentByTag is ScreenShareFragment) {
            returnToCamera()
            super.onBackPressed()
            Log.i(TAG, "onBackPressed")
        }
    }

    private fun returnToCamera() {
        try {
            currentSession!!.mediaStreamManager.videoCapturer = QBRTCCameraVideoCapturer(this, null)
        } catch (e: QBRTCCameraVideoCapturer.QBRTCCameraCapturerException) {
            Log.i(TAG, "Error: device doesn't have camera")
        }
    }



    private fun hangUpCurrentSession() {
        Log.d(TAG, "hangUpCurrentSession")
        if (currentSession != null) {
            Log.d(TAG, "hangUpCurrentSession currentSession != null")
            currentSession!!.hangUp(HashMap<String, String>())
        }
    }


    //***********************************************************************************************************
    // conform to ScreenShareFragment OnSharingEvents
    //***********************************************************************************************************


    override fun onStopSharingPreview() {
        onBackPressed()
    }


    //***********************************************************************************************************
    // conform to PreviewCallFragment.CallFragmentCallbackListener
    //***********************************************************************************************************
    override fun onStartCall(session: QBRTCSession) {
        Log.d(TAG, "onStartCall = $session")

        initCurrentSession(session)
//        startAudioManager()

       initConversationFragment(false)
    }

    override fun onAcceptCall() {
        Log.d(TAG, "onAcceptCall")
//        startAudioManager()
        initConversationFragment(true)
    }

    override fun onRejectCall() {
        if (currentSession != null) {
            currentSession!!.rejectCall(null)
        }
    }

    override fun onLogout() {
        startLogout()
        finish()
    }

    //***********************************************************************************************************
    // conform to VideoConversationFragment.CallFragmentCallbackListener
    //***********************************************************************************************************


    override fun onHangUpCall() {
        hangUpCurrentSession()
    }

    override fun onSwitchAudio() {
        Log.v(TAG, "onSwitchAudio(), SelectedAudioDevice() = " + audioManager!!.selectedAudioDevice)
        if (audioManager!!.selectedAudioDevice != AppRTCAudioManager.AudioDevice.SPEAKER_PHONE) {
            audioManager!!.selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)
        } else {
            if (audioManager!!.audioDevices.contains(AppRTCAudioManager.AudioDevice.BLUETOOTH)) {
                audioManager!!.selectAudioDevice(AppRTCAudioManager.AudioDevice.BLUETOOTH)
            } else if (audioManager!!.audioDevices.contains(AppRTCAudioManager.AudioDevice.WIRED_HEADSET)) {
                audioManager!!.selectAudioDevice(AppRTCAudioManager.AudioDevice.WIRED_HEADSET)
            } else {
                audioManager!!.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE)
            }
        }
    }

    override fun onStartScreenSharing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }
        QBRTCScreenCapturer.requestPermissions(this@VideoCallingActivity)
    }

    override fun onSwitchCamera(cameraSwitchHandler: CameraVideoCapturer.CameraSwitchHandler) {
        (currentSession!!.mediaStreamManager.videoCapturer as QBRTCCameraVideoCapturer)
            .switchCamera(cameraSwitchHandler)
    }

    // Below atleast 8 methods to conform QBRTCSessionEventsCallback && QBRTCClientSessionCallbacks QBRTCSession interfaces
    //QBRTCClientSessionCallbacks
    override fun onSessionStartClose(session: QBRTCSession) {
        Log.d(TAG, "onSessionStartClose")
        if (session == currentSession) {
            updatePreviewCallButtons(false)
        }
    }

    override fun onReceiveNewSession(session: QBRTCSession) {
        Log.d(TAG, "onReceiveNewSession")
        if (currentSession == null) {
            currentSession = session
            updatePreviewCallButtons(true)
        } else {
            Log.d(TAG, "Stop new session. Device now is busy")
            session.rejectCall(null)
        }
    }

    override fun onUserNoActions(session: QBRTCSession, userId: Int) {
    }

    //    QBRTCSessionEventsCallback
    override fun onReceiveHangUpFromUser(session: QBRTCSession, userId: Int, userInfo: MutableMap<String, String>?) {
        Log.d(TAG, "onReceiveHangUpFromUser")
        fun getUserNameOrLogin(userId: Int): String {
//            opponents.forEach { if (it.id == userId) return it.fullName ?: it.login }
            if(opponent.id == userId) {
                return opponent.fullName ?: opponent.login
            }else{
                return ""
            }

        }
        Toast.makeText(applicationContext, "User " + getUserNameOrLogin(userId) + " " + getString(R.string.text_status_hang_up), Toast.LENGTH_SHORT).show()
    }

    override fun onCallAcceptByUser(session: QBRTCSession?, p1: Int?, p2: MutableMap<String, String>?) {
        Log.d(TAG, "onCallAcceptByUser")
    }

    override fun onSessionClosed(session: QBRTCSession) {
        Log.d(TAG, "Session " + session.sessionID)

        if (session.equals(currentSession)) {
            Log.d(TAG, "Stop session")
            audioManager?.stop()
            audioManager = null
            releaseCurrentSession()
            initPreviewFragIfNeed()
        }
    }

    override fun onCallRejectByUser(session: QBRTCSession, userId: Int, userInfo: MutableMap<String, String>?) {
        Log.d(TAG, "onCallRejectByUser")
    }

    override fun onUserNotAnswer(session: QBRTCSession, userId: Int) {

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
