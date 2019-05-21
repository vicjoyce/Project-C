package com.brianku.qbchat.landing_screen.user_login.user_login

import android.content.Intent

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.brianku.qbchat.R
import com.brianku.qbchat.main_section.MainSectionActivity
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        initComponents()
    }

    private fun initComponents(){
        progressBar(false)
        signup_signin_tv.setOnClickListener{
            startActivity(Intent(this, LoginScreenActivity::class.java))
        }

        singup_sign_up_btn.setOnClickListener {
            val login = signup_Login_et.text.toString().trim()
            val password = signup_password_et.text.toString().trim()
            val fullname = signup_fullname_et.text.toString().trim()
            val email = signup_email_et.text.toString().trim()

            if(login.isEmpty() || password.isEmpty() || email.isEmpty() || fullname.isEmpty()){
                Toast.makeText(this,"Please apply the info to register",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

              val user = QBUser(login,password)
              user.fullName = fullname
              user.email = email
              signUpUser(user)
        }
    }

    private fun signUpUser(user: QBUser){
        progressBar(true)
        QBUsers.signUp(user).performAsync(object : QBEntityCallback<QBUser>{
            override fun onSuccess(p0: QBUser?, p1: Bundle?) {
                // Sign up new user in QB then Sign in to next activity
                QBUsers.signIn(user).performAsync(object:QBEntityCallback<QBUser>{
                    override fun onSuccess(qbUser: QBUser?, p1: Bundle?) {
                        // obtain id from session
                        user.id = qbUser?.id
                        progressBar(false)
                        Toast.makeText(this@SignUpActivity,"Sign In Successfully",Toast.LENGTH_LONG).show()
                        goToMainSectionActivity(user)
                    }

                    override fun onError(e: QBResponseException?) {
                        progressBar(false)
                        Toast.makeText(this@SignUpActivity,"Sign In Failed: ${e?.message}",Toast.LENGTH_LONG).show()
                    }
                })
            }

            override fun onError(e: QBResponseException?) {
                progressBar(false)
                Toast.makeText(this@SignUpActivity,"Sign In Failed: ${e?.message}",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun goToMainSectionActivity(user:QBUser){
        val intent = Intent(this@SignUpActivity,MainSectionActivity::class.java)
        intent.putExtra("user",user)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun progressBar(spin:Boolean){
        if(spin){
            sign_up_progressBar.visibility = ProgressBar.VISIBLE
        }else{
            sign_up_progressBar.visibility = ProgressBar.INVISIBLE
        }
    }

}
