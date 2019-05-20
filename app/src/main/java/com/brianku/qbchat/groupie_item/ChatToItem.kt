package com.brianku.qbchat.groupie_item

import com.brianku.qbchat.R
import com.quickblox.chat.model.QBChatMessage
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.row_from_self.view.*

class ChatToItem(val body:String): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.row_from_self
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.row_to_self_tv.text = body
    }
}