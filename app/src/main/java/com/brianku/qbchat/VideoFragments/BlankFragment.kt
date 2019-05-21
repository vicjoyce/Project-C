package com.brianku.qbchat.VideoFragments


import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.fragment.app.Fragment

import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat

import com.brianku.qbchat.R
import com.quickblox.chat.QBChatService
import java.lang.ref.WeakReference


abstract class BaseToolBarFragment : Fragment() {
    private var TAG = BaseToolBarFragment::class.java.simpleName
    lateinit var mainHandler: Handler
    lateinit var actionBar: ActionBar

    protected abstract val fragmentLayout: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        mainHandler = FragmentLifeCycleHandler(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(fragmentLayout, container, false)
        actionBar = (activity as AppCompatActivity).delegate.supportActionBar!!
        initActionBar()
        return view
    }

    open fun initActionBar() {
        actionBar.title = String.format(QBChatService.getInstance().user.login)
        actionBar.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context!!, R.color.grey_600)))
    }


    class FragmentLifeCycleHandler(fragment: Fragment) : Handler() {

        private val fragmentRef: WeakReference<Fragment> = WeakReference(fragment)

        override fun dispatchMessage(msg: Message) {
            val fragment = fragmentRef.get() ?: return
            if (fragment.isAdded && fragment.activity != null) {
                super.dispatchMessage(msg)
            } else {
                Log.d("BaseToolBarFragment", "Fragment under destroying")
            }
        }
    }
}