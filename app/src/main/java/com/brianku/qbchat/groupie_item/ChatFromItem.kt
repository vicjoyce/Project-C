package com.brianku.qbchat.groupie_item

import com.brianku.qbchat.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.row_from_sender.view.*

class ChatFromItem(val item:String): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.row_from_sender
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.row_from_sender_tv.text = item
    }
}