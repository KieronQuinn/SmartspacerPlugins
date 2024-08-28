package com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kieronquinn.app.smartspacer.plugin.googlewallet.PACKAGE_WALLET

class WalletLaunchProxyActivity: AppCompatActivity() {

    companion object {
        private const val EXTRA_POP_UNDER = "pop_under"

        fun createIntent(context: Context, valuableId: String, popUnder: Boolean): Intent {
            return Intent(context, WalletLaunchProxyActivity::class.java).apply {
                data = Uri.parse("https://pay.google.com/gp/v/valuable/$valuableId?vs=gp_lp")
                putExtra(EXTRA_POP_UNDER, popUnder)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data
        val popUnder = intent.getBooleanExtra(EXTRA_POP_UNDER, false)
        val intents = listOfNotNull(
            if(popUnder) packageManager.getLaunchIntentForPackage(PACKAGE_WALLET) else null,
            Intent(Intent.ACTION_VIEW).apply {
                data = uri
                if(popUnder) {
                    addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                }
            }
        ).toTypedArray()
        startActivities(intents)
        finishAndRemoveTask()
    }

}