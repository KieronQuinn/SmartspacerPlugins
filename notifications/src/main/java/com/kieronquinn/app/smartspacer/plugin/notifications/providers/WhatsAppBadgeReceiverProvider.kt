package com.kieronquinn.app.smartspacer.plugin.notifications.providers

import com.kieronquinn.app.smartspacer.plugin.notifications.complications.WhatsAppComplication

class WhatsAppBadgeReceiverProvider: BaseBadgeReceiverProvider() {

    override val packageName = WhatsAppComplication.PACKAGE_NAME

}