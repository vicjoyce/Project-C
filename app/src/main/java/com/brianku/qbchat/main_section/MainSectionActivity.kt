package com.brianku.qbchat.main_section

import android.content.Intent

import android.os.Bundle



import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment

import com.brianku.qbchat.R
import com.brianku.qbchat.landing_screen.user_login.user_login.LoginScreenActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.quickblox.chat.QBChatService
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import kotlinx.android.synthetic.main.activity_main_section.*

class MainSectionActivity : AppCompatActivity() {

    companion object {

        // save the login user data and  LoginScreenActivity will use MainSectionActivity.currentUser to pass back if user session is exist.
        var currentUser:QBUser? = null
    }

    // bottom navigation bar item selected listener
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
        // setup custom action bar
        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setCustomView(R.layout.abs_layout)

        bottom_navigation_bar.setOnNavigationItemSelectedListener(mOnNavigationItemListIntent)
        setUpIntentData()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.logout) {

            // proceed session sign out, chat service sign out
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
                    backToLoginActivity()
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

            // container pass QBuser to fragment

            val user = intent.getSerializableExtra("user") as QBUser
            currentUser = user
            val arguments = Bundle()
            arguments.putSerializable("user",user)
            val fragment = InboxFragment()
            fragment.arguments = arguments
            replaceFragment(fragment)
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer,fragment)
        fragmentTransaction.commit()
    }

    private fun backToLoginActivity(){
        val intent = Intent(this@MainSectionActivity,LoginScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}
