package com.kieronquinn.app.smartspacer.plugin.notifications.providers

import com.kieronquinn.app.smartspacer.plugin.notifications.complications.FacebookComplication

class FacebookBadgeReceiverProvider: BaseBadgeReceiverProvider() {

    override val packageName = FacebookComplication.PACKAGE_NAME

}