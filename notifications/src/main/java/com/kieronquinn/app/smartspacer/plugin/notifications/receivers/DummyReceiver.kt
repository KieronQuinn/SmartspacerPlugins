package com.kieronquinn.app.smartspacer.plugin.notifications.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 *  This receiver doesn't actually do anything, since the badge action will almost never be an
 *  explicit intent. We instead use Smartspacer's Broadcast Provider, but some apps query for
 *  broadcasts with the action to check if any apps will receive calls (even if they actually no
 *  longer will), so this satisfies that requirement.
 */
class DummyReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //No-op
    }
}