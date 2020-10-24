package com.codegames.aparatview

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import kotlinx.android.synthetic.main.dialog_video_enabled_fullscreen.*

internal class VideoEnabledFullScreenDialog(
    context: Context,
    private val target: View,
    private val ratio: String = "16:9",
    val onClose: () -> Unit
) : Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setContentView(R.layout.dialog_video_enabled_fullscreen)

        dvef_fullscreenContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = ratio
        }
        dvef_fullscreenContainer.addView(
            target,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        dvef_dismissArea.setOnClickListener {
            onClose()
            dismiss()
        }

    }

}