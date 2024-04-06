package com.kieronquinn.app.smartspacer.plugin.controls.ui.activities

import android.os.Bundle
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.shared.components.blur.BlurProvider
import com.kieronquinn.monetcompat.app.MonetCompatActivity
import org.koin.android.ext.android.inject

class PopupControlDialogActivity: MonetCompatActivity() {

    private val blurProvider by inject<BlurProvider>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setupWindowFlags()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup_control)
    }

    private fun setupWindowFlags() {
        setShowWhenLocked(true)
    }

    override fun onResume() {
        super.onResume()
        blurProvider.applyBlurToWindow(window, 1f)
    }

    override fun onPause() {
        blurProvider.applyBlurToWindow(window, 0f)
        super.onPause()
    }

}