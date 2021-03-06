package com.brianku.qbchat.VideoFragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.brianku.qbchat.R
import kotlinx.android.synthetic.main.fragment_item_screen_share.*
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class PreviewSharingFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_item_screen_share, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        image_preview.loadUrl(arguments!!.getInt(PREVIEW_IMAGE))
    }

    private fun ImageView.loadUrl(resourceId: Int) {
        Glide.with(this@PreviewSharingFragment.activity)
            .load(resourceId)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override((activity!!.resources.getDimension(R.dimen.pager_image_width)).toInt(),
                (activity!!.resources.getDimension(R.dimen.pager_image_height)).toInt())
            .into(this)
    }

    companion object {

        const val PREVIEW_IMAGE = "preview_image"

        fun newInstance(imageResourceId: Int) =
            PreviewSharingFragment().apply {
                arguments = Bundle().apply {
                    putInt(PREVIEW_IMAGE, imageResourceId)
                }
            }
    }
}
