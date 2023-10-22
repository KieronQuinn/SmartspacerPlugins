package com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.activities

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.kieronquinn.app.smartspacer.plugin.googlewallet.R
import com.kieronquinn.app.smartspacer.plugin.shared.components.blur.BlurProvider
import com.kieronquinn.monetcompat.app.MonetCompatActivity
import org.koin.android.ext.android.inject

class PopupWalletDialogActivity: MonetCompatActivity() {

    private val blurProvider by inject<BlurProvider>()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setupWindowFlags()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup_wallet)
    }

    private fun Window.setupWindowFlags(){
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
    }

    override fun onResume() {
        super.onResume()
        window.attributes.screenBrightness = 1f
        blurProvider.applyBlurToWindow(window, 1f)
    }

    override fun onPause() {
        blurProvider.applyBlurToWindow(window, 0f)
        super.onPause()
    }

}