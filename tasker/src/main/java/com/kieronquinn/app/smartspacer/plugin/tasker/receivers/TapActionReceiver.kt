package com.kieronquinn.app.smartspacer.plugin.tasker.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTapActionEventInput
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.TapActionEventActivity

class TapActionReceiver: BroadcastReceiver() {

    companion object {
        private const val EXTRA_ID = "id"

        fun createIntent(context: Context, id: String): Intent {
            return Intent(context, TapActionReceiver::class.java).apply {
                putExtra(EXTRA_ID, id)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        TapActionEventActivity::class.java.requestQuery(context, SmartspacerTapActionEventInput(id))
    }

}