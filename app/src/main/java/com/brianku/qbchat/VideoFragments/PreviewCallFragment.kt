package com.brianku.qbchat.VideoFragments


import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.brianku.qbchat.R
import com.quickblox.users.model.QBUser


class PreviewCallFragment : BaseToolBarFragment() {

    override val fragmentLayout: Int
        get() = R.layout.fragment_preview_call

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_preview_call, container, false)
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
