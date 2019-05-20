package com.brianku.qbchat.main_section

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.brianku.qbchat.R
import com.brianku.qbchat.landing_screen.user_login.user_login.LoginScreenActivity
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.chat.QBChatService
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import kotlinx.android.synthetic.main.activity_main_section.*

class MainSectionActivity : AppCompatActivity() {

    companion object {
        var currentUser:QBUser? = null
    }


    private val mOnNavigationItemListIntent = BottomNavigationView.OnNavigationItemSelectedListener{
        when(it.itemId){
            R.id.inbox_list ->{
                replaceFragment(InboxFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.friend_list ->{
                replaceFragment(FriendListFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.user_profile->{
                replaceFragment(UserProfileFragment())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_section)
        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setCustomView(R.layout.abs_layout)

        bottom_navigation_bar.setOnNavigationItemSelectedListener(mOnNavigationItemListIntent)
        setUpIntentData()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.logout) {

            // proceed sign out, sign out from qbusers and destroy chat service
            QBUsers.signOut().performAsync(object:QBEntityCallback<Void>{
                override fun onSuccess(p0: Void?, p1: Bundle?) {
                    val chatService = QBChatService.getInstance()
                    if(chatService.isLoggedIn){
                        chatService.logout(object :QBEntityCallback<Void>{
                            override fun onSuccess(p0: Void?, p1: Bundle?) {
                                chatService.destroy()
                            }
                            override fun onError(p0: QBResponseException?) {}
                        })
                    }
                    Toast.makeText(this@MainSectionActivity,"Sign out successfully",Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@MainSectionActivity,LoginScreenActivity::class.java))
                    finish()
                }

                override fun onError(e: QBResponseException?) {
                    Toast.makeText(this@MainSectionActivity,"Sign out Invalid: ${e?.message}",Toast.LENGTH_LONG).show()
                }
            })

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }


    private fun setUpIntentData(){
            val user = intent.getSerializableExtra("user") as QBUser
            currentUser = user
            val arguments = Bundle()
            arguments.putSerializable("user",user)
            val fragment = InboxFragment()
            fragment.arguments = arguments
            replaceFragment(fragment)
    }

    private fun replaceFragment(fragment:Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer,fragment)
        fragmentTransaction.commit()
    }

}
