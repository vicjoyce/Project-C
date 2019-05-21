package com.brianku.qbchat.utils



import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator


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
