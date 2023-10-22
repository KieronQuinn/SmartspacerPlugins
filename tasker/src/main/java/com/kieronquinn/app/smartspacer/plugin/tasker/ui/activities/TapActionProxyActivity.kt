package com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTapActionEventInput

class TapActionProxyActivity: Activity() {

    companion object {
        private const val EXTRA_ID = "id"

        fun createIntent(context: Context, id: String): Intent {
            return Intent(context, TapActionProxyActivity::class.java).apply {
                putExtra(EXTRA_ID, id)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        TapActionEventActivity::class.java.requestQuery(this, SmartspacerTapActionEventInput(id))
        finish()
    }

}