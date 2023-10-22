package com.kieronquinn.app.smartspacer.plugin.notifications.providers

import com.kieronquinn.app.smartspacer.plugin.notifications.complications.ThreadsComplication

class ThreadsBadgeReceiverProvider: BaseBadgeReceiverProvider() {

    override val packageName = ThreadsComplication.PACKAGE_NAME

}