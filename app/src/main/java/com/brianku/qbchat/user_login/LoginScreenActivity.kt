package com.brianku.qbchat.landing_screen.user_login.user_login

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import com.brianku.qbchat.R
import com.brianku.qbchat.extension.hideKeyboard
import com.brianku.qbchat.main_section.MainSectionActivity
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.auth.session.QBSessionParameters
import com.quickblox.auth.session.QBSettings
import com.quickblox.chat.QBChatService
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import kotlinx.android.synthetic.main.activity_login_screen.*

class LoginScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)
        initFramework()
        initComponents()
    }

    override fun onStart() {
        super.onStart()
        if(QBSessionManager.getInstance().sessionParameters != null
            && QBChatService.getInstance().isLoggedIn
            && MainSectionActivity.currentUser != null){
            goToMainSectionWithUser(MainSectionActivity.currentUser!!)
        }
    }

    private fun initComponents(){
        progressBar(false)
        loginscreen_signup_tv.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        loginscreen_sign_in_btn.setOnClickListener {
            val login = loginscreen_username_et.text.toString().trim()
            val password = loginscreen_password_et.text.toString().trim()
            if(login.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"Email or Password must be applied !!",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            loginscreen_username_et.hideKeyboard()
            loginscreen_password_et.hideKeyboard()
            val user = QBUser(login,password)
            loginUser(user)
        }
    }

    private fun loginUser(user:QBUser){
        progressBar(true)
        QBUsers.signIn(user).performAsync(object :QBEntityCallback<QBUser>{
            override fun onSuccess(qbUser: QBUser?, p1: Bundle?) {
                progressBar(false)

                // obtain user.id, qbuser which passed into Onsucess doesn't have password, it can't be used to login directly.
                // we need user with login/email, id and password to login chat server
                user.id = qbUser!!.id
                Toast.makeText(this@LoginScreenActivity,"Login Successfully",Toast.LENGTH_LONG).show()
                goToMainSectionWithUser(user)
            }

            override fun onError(e: QBResponseException?) {
                progressBar(false)
                Toast.makeText(this@LoginScreenActivity,"***LOGIN ERROR: ${e?.message} ***",Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun initFramework(){
        // initialize framework with application credentials
        QBSettings.getInstance().init(this, APP_ID, AUTH_KEY, AUTH_SECRET)
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY)
    }

    private fun progressBar(spin:Boolean){
        if(spin){
            login_progressBar.visibility = ProgressBar.VISIBLE
        }else{
            login_progressBar.visibility = ProgressBar.INVISIBLE
        }
    }

    private fun goToMainSectionWithUser(user:QBUser){
        val intent = Intent(this@LoginScreenActivity,MainSectionActivity::class.java)
        intent.putExtra("user",user)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    companion object {
        val APP_ID = "77008"
        val AUTH_KEY = "H9GTttgdCJpXZ3e"
        val AUTH_SECRET = "e2Ln-vbEy9daW7W"
        val ACCOUNT_KEY = "sy_rmDxxG5dU7EUBpda9"
    }
}

