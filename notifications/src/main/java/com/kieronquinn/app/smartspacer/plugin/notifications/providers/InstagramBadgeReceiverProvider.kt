package com.kieronquinn.app.smartspacer.plugin.notifications.providers

import com.kieronquinn.app.smartspacer.plugin.notifications.complications.InstagramComplication

class InstagramBadgeReceiverProvider: BaseBadgeReceiverProvider() {

    override val packageName = InstagramComplication.PACKAGE_NAME

}