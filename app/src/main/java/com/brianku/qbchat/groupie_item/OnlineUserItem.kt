package com.brianku.qbchat.groupie_item

import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.brianku.qbchat.R
import com.brianku.qbchat.utils.textDrawable
import com.quickblox.users.model.QBUser
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.online_users_list.view.*

class OnlineUserItem(val item: QBUser): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.online_users_list
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.online_user_username.text = item.fullName
        val drawable = textDrawable(item.fullName)
        viewHolder.itemView.online_user_circleImageView.setImageDrawable(drawable)
    }
}