package com.brianku.qbchat.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.brianku.qbchat.main_section.MainSectionActivity
import com.quickblox.users.model.QBUser

fun textDrawable(string:String): TextDrawable{
    val generator = ColorGenerator.MATERIAL
    val randomColor = generator.randomColor
    val builder = TextDrawable.builder().beginConfig()
        .withBorder(4)
        .endConfig()
        .round()
    val drawable = builder.build(string.substring(0,1).toUpperCase(),randomColor)
    return drawable
}
