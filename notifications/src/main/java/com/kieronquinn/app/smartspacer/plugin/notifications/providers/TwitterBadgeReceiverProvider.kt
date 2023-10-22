package com.kieronquinn.app.smartspacer.plugin.notifications.providers

import com.kieronquinn.app.smartspacer.plugin.notifications.complications.TwitterComplication

class TwitterBadgeReceiverProvider: BaseBadgeReceiverProvider() {

    override val packageName = TwitterComplication.PACKAGE_NAME

}