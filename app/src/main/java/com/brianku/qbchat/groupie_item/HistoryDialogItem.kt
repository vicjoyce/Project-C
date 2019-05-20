package com.brianku.qbchat.groupie_item

import com.brianku.qbchat.R
import com.brianku.qbchat.utils.textDrawable
import com.quickblox.chat.model.QBChatDialog
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.history_chat.view.*

class HistoryDialogItem(val item: QBChatDialog): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.history_chat
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
              viewHolder.itemView.dialog_last_message.text = item.lastMessage
              viewHolder.itemView.dialog_name.text = item.name
              viewHolder.itemView.dialog_last_message_date_sent.text = item.lastMessageDateSent.toString()
              val drawable = textDrawable(item.name)
              viewHolder.itemView.dialog_photo_circleImageView.setImageDrawable(drawable)
    }
}